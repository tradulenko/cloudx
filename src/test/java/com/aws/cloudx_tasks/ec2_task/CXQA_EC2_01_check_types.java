package com.aws.cloudx_tasks.ec2_task;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@RunWith(Parameterized.class)
public class CXQA_EC2_01_check_types extends AbstractTest {

    @Parameterized.Parameter
    public String ec2NameExpected;

    @Parameterized.Parameter(1)
    public boolean shouldHavePublicIPExpected;

    @Parameterized.Parameters(name = "{index}: params={0},{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"cloudxinfo/PrivateInstance/Instance", false},
                {"cloudxinfo/PublicInstance/Instance", true}
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

        if (shouldHavePublicIPExpected) {
            Assert.assertNotNull(ec2NameExpected + " should have public IP ", actualInstance.get().publicIpAddress());
        } else {
            Assert.assertNull(ec2NameExpected + " should not have public IP ", actualInstance.get().publicIpAddress());
        }
    }
}
