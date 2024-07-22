package com.aws.cloudx_tasks.vpc_task;

import com.aws.cloudx_tasks.ec2_task.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RunWith(Parameterized.class)
public class CXQA_VPC_01_check_configuration extends AbstractTest {


    @Parameterized.Parameter
    public String ec2NameExpected;

    @Parameterized.Parameter(1)
    public String vpnCidrExpected;

    @Parameterized.Parameter(2)
    public String tagValueExpected;

    @Parameterized.Parameter(3)
    public boolean shouldHavePublicIPExpected;

    @Parameterized.Parameters(name = "{index}: params={0},{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"cloudxinfo/PrivateInstance/Instance", "10.0.0.0/16", "cloudx:qa", false},
                {"cloudxinfo/PublicInstance/Instance", "10.0.0.0/16", "cloudx:qa", true}
        });
    }

    @Test
    public void checkEC2types() {
        DescribeInstancesResponse response = ec2.describeInstances();

        Optional<Instance> actualInstance = response.reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .filter(instance -> instance.tags().stream().anyMatch(tag ->
                        tag.key().equals("Name") && tag.value().equals(ec2NameExpected)))
                .findFirst();

        if (actualInstance.isPresent()) {
            System.out.println("Found the expected instance: " + actualInstance.get().instanceId());
        } else {
            Assert.fail("The expected instance was not found. ExpectedInstanceName is " + ec2NameExpected);
        }

        Assert.assertEquals("Tag is not expected", tagValueExpected.split(":")[1], getTag(actualInstance.get()).value());

        if (shouldHavePublicIPExpected) {
            Assert.assertNotNull(ec2NameExpected + " should have public IP ", actualInstance.get().publicIpAddress());
        } else {
            Assert.assertNull(ec2NameExpected + " should not have public IP ", actualInstance.get().publicIpAddress());
        }

        // check VPC
        String vpcId = actualInstance.get().vpcId();
        String vpcCidrBlock = ec2.describeVpcs(r -> r.vpcIds(vpcId)).vpcs().get(0).cidrBlock();
        Assert.assertEquals("VPC CIDR block is not expected", vpnCidrExpected, vpcCidrBlock);


        // Check VPC is not default
        DescribeVpcsResponse vpcsResponse = ec2.describeVpcs(DescribeVpcsRequest.builder().vpcIds(vpcId).build());
        Vpc vpc = vpcsResponse.vpcs().get(0);
        Assert.assertFalse("VPC should not be default", vpc.isDefault());

        // Check VPC has two subnets: public and private
        DescribeSubnetsResponse subnetsResponse = ec2.describeSubnets(DescribeSubnetsRequest.builder().filters(
                Filter.builder().name("vpc-id").values(vpcId).build()).build());
        List<Subnet> subnets = subnetsResponse.subnets();
        Assert.assertEquals("VPC should have 2 subnets", 2, subnets.size());

        boolean isPublicSubnet = false;
        boolean isPrivateSubnet = false;
        for (Subnet subnet : subnets) {
            boolean hasInternetGateway = subnet.mapPublicIpOnLaunch(); // Simplified check, may not be accurate
            if (hasInternetGateway) {
                isPublicSubnet = true;
            } else {
                isPrivateSubnet = true;
            }
        }
        Assert.assertTrue("VPC should have public subnet", isPublicSubnet);
        Assert.assertTrue("VPC should have private subnet", isPrivateSubnet);

        // Check VPC tags
        String tagNameExpected = tagValueExpected.split(":")[0];
        String tagValueExpected1 = tagValueExpected.split(":")[1];

        Optional<Tag> vpcTag = vpc.tags().stream()
                .filter(tag -> tag.key().equals(tagNameExpected) && tag.value().equals(tagValueExpected1))
                .findFirst();

        if (vpcTag.isPresent()) {
            System.out.println("VPC has the correct tag.");
        } else {
            Assert.fail("VPC does not have the correct tag.");
        }

    }

    private Tag getTag(Instance instance) {
        String tagName = tagValueExpected.split(":")[0];
        Optional<Tag> instanceTag = instance.tags().stream()
                .filter(tag -> tag.key().equals(tagName))
                .findFirst();
        if (instanceTag.isPresent()) {
            System.out.println("Instance has the correct tag.");
        } else {
            Assert.fail("Instance does not have the correct tag.");
        }
        return instanceTag.get();
    }
}
