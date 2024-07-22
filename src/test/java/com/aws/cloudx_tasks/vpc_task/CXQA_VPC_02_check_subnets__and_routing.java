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
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class CXQA_VPC_02_check_subnets__and_routing extends AbstractTest {


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
                {"cloudxinfo/PublicInstance/Instance", "10.0.0.0/16", "cloudx:qa", true}
        });
    }

    @Test
    public void checkVPCConfig() {
        // Get instance's VPC
        DescribeInstancesResponse response = ec2.describeInstances();
        Instance instance = getInstanceByName(response, "cloudxinfo/PublicInstance/Instance");
        String vpcId = instance.vpcId();
        System.out.println("vpcId = " + vpcId);

        // Check VPC has Internet Gateway
        DescribeInternetGatewaysResponse igwResponse = ec2.describeInternetGateways();
        boolean hasIgw = igwResponse.internetGateways().stream()
                .anyMatch(igw -> igw.attachments().stream()
                        .anyMatch(attachment -> attachment.vpcId().equals(vpcId) && attachment.stateAsString().equals("available"))
                );


        Assert.assertTrue("VPC should have an Internet Gateway", hasIgw);

        // Check VPC's route tables contain routes to IGW and NAT
        DescribeRouteTablesResponse routeTablesResponse = ec2.describeRouteTables(DescribeRouteTablesRequest.builder().filters(
                Filter.builder().name("vpc-id").values(vpcId).build()).build());
        List<RouteTable> routeTables = routeTablesResponse.routeTables();

        boolean hasIgwRoute;
        boolean hasNatRoute;


        hasIgwRoute = routeTables.stream().anyMatch(routeTable ->
                routeTable.routes().stream()
                        .anyMatch(route -> {
                            System.out.println("--- " + route.gatewayId() + " " + route.stateAsString());
                            return (route.gatewayId() != null) && (route.gatewayId().contains("igw-") && route.stateAsString().equals(RouteState.ACTIVE.toString()));
                        }));
        hasNatRoute = routeTables.stream().anyMatch(routeTable -> routeTable.routes().stream()
                .anyMatch(route -> (route.natGatewayId() != null && route.stateAsString().equals(RouteState.ACTIVE.toString()))));


        Assert.assertTrue("Route table should have a route to an Internet Gateway", hasIgwRoute);
        Assert.assertTrue("Route table should have a route to a NAT Gateway", hasNatRoute);


        Instance privateInstance = getInstanceByName(response, "cloudxinfo/PrivateInstance/Instance");
        // Check private instance is not accessible from the internet
        List<String> privateSecurityGroups = privateInstance.securityGroups().stream()
                .map(GroupIdentifier::groupId)
                .toList();

        boolean isPrivateInstanceAccessibleFromInternet = false;
        for (SecurityGroup sg : ec2.describeSecurityGroups().securityGroups()) {
            if (privateSecurityGroups.contains(sg.groupId())) {
                isPrivateInstanceAccessibleFromInternet = sg.ipPermissions().stream()
                        .flatMap(ipPerm -> ipPerm.ipRanges().stream())
                        .anyMatch(ipRange -> ipRange.cidrIp().equals("0.0.0.0/0"));
            }
        }

        Assert.assertFalse("Private instance should not be accessible from the internet", isPrivateInstanceAccessibleFromInternet);

        // Check private instance has access to the internet via NAT Gateway
        DescribeRouteTablesResponse routeTablesResponse1 = ec2.describeRouteTables(DescribeRouteTablesRequest.builder().build());
        boolean privateSubnetHasNatGateway = false;
        for (RouteTable rt : routeTablesResponse1.routeTables()) {
            for (Route route : rt.routes()) {
                if (route.natGatewayId() != null && RouteState.ACTIVE.toString().equals(route.stateAsString())) {
                    for (RouteTableAssociation assoc : rt.associations()) {
                        if (assoc.subnetId().equals(privateInstance.subnetId())) {
                            privateSubnetHasNatGateway = true;
                            break;
                        }
                    }
                }
            }
        }

        Assert.assertTrue("Private instance's subnet should be associated with a Route Table that has a route to a NAT Gateway", privateSubnetHasNatGateway);
    }

    private Instance getInstanceByName(DescribeInstancesResponse response, String instanceName) {
        Optional<Instance> actualInstance = response.reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .filter(instance -> instance.tags().stream().anyMatch(tag ->
                        tag.key().equals("Name") && tag.value().equals(instanceName)))
                .findFirst();

        if (actualInstance.isPresent()) {
            System.out.println("Found the expected instance: " + actualInstance.get().instanceId());
        } else {
            Assert.fail("The expected instance was not found. ExpectedInstanceName is " + ec2NameExpected);
        }
        return actualInstance.get();
    }
}
