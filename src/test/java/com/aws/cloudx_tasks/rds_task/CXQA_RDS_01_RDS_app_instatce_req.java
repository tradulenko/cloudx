package com.aws.cloudx_tasks.rds_task;

import org.junit.Test;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.Subnet;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CXQA_RDS_01_RDS_app_instatce_req extends AbstractTest {


    @Test
    public void checkRdsInstanceRequirements() {
        Ec2Client ec2 = Ec2Client.create();

        DescribeDbInstancesResponse dbInstancesResponse = rdsClient.describeDBInstances();

        for (DBInstance dbInstance : dbInstancesResponse.dbInstances()) {
            String dbSubnetGroupName = dbInstance.dbSubnetGroup().dbSubnetGroupName();

            boolean isPrivate = true;
            for (Subnet subnet : dbInstance.dbSubnetGroup().subnets()) {

                DescribeRouteTablesResponse rts = ec2.describeRouteTables(
                        DescribeRouteTablesRequest.builder()

                                .filters(Filter.builder().name("association.subnet-id").values(subnet.subnetIdentifier()).build())
                                .build());

                for (RouteTable rt : rts.routeTables()) {
                    for (Route route : rt.routes()) {
                        if (route.gatewayId() != null && route.gatewayId().startsWith("igw-")) {
                            System.out.println("Public Subnet found: " + subnet.subnetIdentifier());
                            isPrivate = false;
                            break;
                        }
                    }
                    if (!isPrivate) break;
                }
                if (!isPrivate) break;
            }
            assert isPrivate : "RDS Instance is not in a private subnet.";
        }
    }

    @Test
    public void dbInstanceSecurityGroupTest() {
        Ec2Client ec2 = Ec2Client.create();

        DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
        for (DBInstance instance : response.dbInstances()) {
            instance.vpcSecurityGroups().forEach(vpcSecGroup -> {
                DescribeSecurityGroupsResponse secGroupResponse = ec2.describeSecurityGroups(
                        DescribeSecurityGroupsRequest.builder().groupIds(vpcSecGroup.vpcSecurityGroupId()).build());

                for (SecurityGroup group : secGroupResponse.securityGroups()) {
                    // Перевірка правил доступу
                    group.ipPermissions().forEach(perm -> {
                        perm.ipRanges().stream().filter(ipRange -> ipRange.cidrIp().equals("REPLACE_PUBLIC_SUBNET_CIDR")
                        ).findFirst().ifPresent(ipRange ->
                                System.out.println("Access allowed from application public subnet to RDS instance.")
                        );
                    });
                }
            });
        }
    }


    @Test
    public void dbInstanceInternetAccessTest() {
        Ec2Client ec2 = Ec2Client.create();

        DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
        for (DBInstance instance : response.dbInstances()) {
            instance.vpcSecurityGroups().forEach(vpcSecGroup -> {
                DescribeSecurityGroupsResponse secGroupResponse = ec2.describeSecurityGroups(
                        DescribeSecurityGroupsRequest.builder().groupIds(vpcSecGroup.vpcSecurityGroupId()).build());

                secGroupResponse.securityGroups().forEach(group -> {
                    boolean accessFromPublicInternet = group.ipPermissions().stream()
                            .flatMap(perm -> perm.ipRanges().stream())
                            .anyMatch(ipRange -> ipRange.cidrIp().equals("0.0.0.0/0"));

                    assertFalse(accessFromPublicInternet, "RDS instance should not be accessible from the public internet.");

                });
            });
        }
    }

}
