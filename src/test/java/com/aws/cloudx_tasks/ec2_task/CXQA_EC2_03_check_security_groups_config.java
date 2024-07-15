package com.aws.cloudx_tasks.ec2_task;

import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Optional;


public class CXQA_EC2_03_check_security_groups_config extends AbstractTest {

    final String privateInstanceName = "cloudxinfo/PrivateInstance/Instance";
    final String publicInstanceName = "cloudxinfo/PublicInstance/Instance";

    @Test
    public void checkEC2types() {
        DescribeInstancesResponse response = ec2.describeInstances();

        Instance privateInstance = getInstanceId(response, privateInstanceName);
        Instance publicInstance = getInstanceId(response, publicInstanceName);

        // Fetch the Security Group Id for each Instance
        String privateSecurityGroupId = privateInstance.securityGroups().get(0).groupId();

        String publicSecurityGroupId = publicInstance.securityGroups().get(0).groupId();

        // Fetch the Public IP of the Public Instance
        String publicIp = publicInstance.publicIpAddress();

        // Describe Security Groups to check the rules
        DescribeSecurityGroupsResponse securityGroupsResponse = ec2.describeSecurityGroups(DescribeSecurityGroupsRequest.builder()
                .groupIds(privateSecurityGroupId, publicSecurityGroupId).build());

        for (SecurityGroup group : securityGroupsResponse.securityGroups()) {
            for (IpPermission perm : group.ipPermissions()) {
                int fromPort = perm.fromPort();
                int toPort = perm.toPort();

                // Check access for Public Instance from the internet
                if (group.groupId().equals(publicSecurityGroupId)) {
                    boolean hasSSHAndHttpAccess = perm.ipRanges().stream().anyMatch(ipRange ->
                            (fromPort == 22 && toPort == 22 || fromPort == 80 && toPort == 80) && ipRange.cidrIp().equals("0.0.0.0/0"));
                    Assert.assertTrue("Public instance  is not accessible from the internet", hasSSHAndHttpAccess);
                    if (hasSSHAndHttpAccess) {
                        System.out.println("Public instance is accessible from the internet through port: " + fromPort);
                    }
                }

                // Check access for Private Instance from Public Instance
                if (group.groupId().equals(privateSecurityGroupId)) {
                    boolean hasCorrespondingProtocolAndIp = perm.userIdGroupPairs().stream().anyMatch(pair ->
                            (fromPort == 22 && toPort == 22 || fromPort == 80 && toPort == 80) && pair.groupId().equals(publicSecurityGroupId));
                    Assert.assertTrue("Private instance is not accessible from public instance " + publicIp, hasCorrespondingProtocolAndIp);
                    if (hasCorrespondingProtocolAndIp) {
                        System.out.println("Private instance is accessible from public instance through port " + fromPort);
                    }
                }
            }
        }
    }


    private Instance getInstanceId(DescribeInstancesResponse response, String instanceName) {
        Optional<Instance> instanceOptional = response.reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .filter(instance -> instance.tags().stream().anyMatch(tag ->
                        tag.key().equals("Name") && tag.value().equals(instanceName)))
                .findFirst();

        Assert.assertTrue("Instance with name " + instanceName + " was not found", instanceOptional.isPresent());

        return instanceOptional.get();
    }

}
