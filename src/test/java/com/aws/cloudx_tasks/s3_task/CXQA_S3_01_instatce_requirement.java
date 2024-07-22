package com.aws.cloudx_tasks.s3_task;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CXQA_S3_01_instatce_requirement extends AbstractTest {

    @Test
    public void checkEC2types() throws IOException {
        DescribeInstancesResponse response = ec2.describeInstances();

        List<Instance> instanceList = response.reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .toList();

        if (instanceList.size() == 1) {
            System.out.println("Found the expected instance: " + instanceList.get(0).instanceId());
        } else {
            Assert.fail("The expected instance was not found.  List of instances  " + instanceList);
        }

        Instance actualInstance = instanceList.get(0);
        String publicIpAddress = actualInstance.publicIpAddress();
        System.out.println(publicIpAddress);
        String publicDnsName = actualInstance.publicDnsName();
        System.out.println(publicDnsName);


        Assert.assertNotNull(publicIpAddress, "The instance public IP address should not be null");
        Assert.assertNotNull(publicDnsName, "The instance public DNS name should not be null");

// Then, check that the subnet of the instance is public
        String subnetIdInstance = actualInstance.subnetId();

        DescribeSubnetsResponse subnetResponse = ec2.describeSubnets(DescribeSubnetsRequest.builder()
                .subnetIds(subnetIdInstance)
                .build());

        Assert.assertEquals("Number of subnet ", 1, subnetResponse.subnets().size());

        Subnet subnetInstance = subnetResponse.subnets().get(0);
        String routeTableId = ec2.describeRouteTables(DescribeRouteTablesRequest.builder()
                        .filters(Filter.builder()
                                .name("association.subnet-id")
                                .values(subnetInstance.subnetId())
                                .build())
                        .build())
                .routeTables().get(0).routeTableId();

        RouteTable routeTable = ec2.describeRouteTables(DescribeRouteTablesRequest.builder()
                        .routeTableIds(routeTableId)
                        .build())
                .routeTables().get(0);

        Assert.assertTrue("The application instance should be in a subnet with a route to an IGW (i.e., public subnet)",
                routeTable.routes().stream()
                        .anyMatch(route -> route.gatewayId() != null && route.gatewayId().startsWith("igw-"))
        );


        // Check access from internet by IP
        URL url = new URL("http://" + publicIpAddress + "/api/image");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        System.out.println("URL " + url.toString());
        int responseCode = connection.getResponseCode();
        Assert.assertEquals("The application should be accessible via HTTP", 200, responseCode);

        // Check access from internet by publicDnsName
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://" + publicDnsName + "/api/image");
        CloseableHttpResponse responseDNS = httpClient.execute(request);
        System.out.println("URL " + "http://" + publicDnsName + "/api/image");
        Assert.assertEquals("The application should be accessible via HTTP", 200, responseDNS.getStatusLine().getStatusCode());


        //SSH
        DescribeSecurityGroupsResponse describeSecurityGroupsResponse = ec2.describeSecurityGroups(DescribeSecurityGroupsRequest.builder()
                .groupIds(actualInstance.securityGroups().get(0).groupId())
                .build());

        boolean sshAllowed = false;
        for (SecurityGroup sg : describeSecurityGroupsResponse.securityGroups()) {
            for (IpPermission perm : sg.ipPermissions()) {
                if (perm.fromPort() != null && perm.fromPort() == 22 && perm.ipProtocol().equalsIgnoreCase("tcp")) {
                    for (IpRange range : perm.ipRanges()) {
                        // Change this if you want to check a specific IP instead of the whole Internet
                        if (range.cidrIp().equals("0.0.0.0/0")) {
                            sshAllowed = true;
                            break;
                        }
                    }
                }
            }
        }

        Assert.assertTrue("The application instance should be accessible by SSH protocol", sshAllowed);

        // S3 bucket
        // Отримайте IAM роль

        String instanceProfileArn = actualInstance.iamInstanceProfile().arn();
        String instanceProfileName = instanceProfileArn.split("/")[1];
        System.out.println("InstanceProfileName: " + instanceProfileName);


// Діставання деталей ролі
        IamClient iam = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();

        GetInstanceProfileResponse response1 = iam.getInstanceProfile(GetInstanceProfileRequest.builder().instanceProfileName(instanceProfileName).build());
        String roleName = response1.instanceProfile().roles().get(0).roleName();

        System.out.println(roleName);


        GetRoleResponse roleResponse = iam.getRole(GetRoleRequest.builder().roleName(roleName).build());

// Ітерація через вкладену політику і перевірка на доступ до S3
// Перебирає усі вбудовані політики ролі
        ListRolePoliciesResponse listRolePoliciesResponse = iam.listRolePolicies(ListRolePoliciesRequest.builder().roleName(roleName).build());
        List<String> policyNames = listRolePoliciesResponse.policyNames();

        ListAttachedRolePoliciesResponse attachedPoliciesResponse = iam.listAttachedRolePolicies(ListAttachedRolePoliciesRequest.builder().roleName(roleName).build());
        List<AttachedPolicy> attachedPolicies = attachedPoliciesResponse.attachedPolicies();
        for (AttachedPolicy policy : attachedPolicies) {
            System.out.println(policy.policyName());
        }

        boolean s3Access = false;

        // Check inline policies
        for (String policyName : policyNames) {
            // Get the policy content
            GetRolePolicyResponse policyResponse = iam.getRolePolicy(GetRolePolicyRequest.builder().roleName(roleName).policyName(policyName).build());
            String policyDocument = policyResponse.policyDocument();

            String decodedPolicyDocument = URLDecoder.decode(policyDocument, StandardCharsets.UTF_8.toString());
            System.out.println("decodedPolicyDocument  " + decodedPolicyDocument);
            // Check if the policy contains S3 access permissions
            if (decodedPolicyDocument.contains("s3:PutObject*") && decodedPolicyDocument.contains("s3:DeleteObject*") && decodedPolicyDocument.contains("s3:GetObject*")) {
                s3Access = true;
                break;
            }
        }

// Check managed policies if no S3 access found yet
        System.out.println("Check managed policies if no S3 access found yet");
        if (!s3Access) {
            for (AttachedPolicy policy : attachedPolicies) {
                GetPolicyResponse getPolicyResponse = iam.getPolicy(GetPolicyRequest.builder().policyArn(policy.policyArn()).build());
                String policyVersionId = getPolicyResponse.policy().defaultVersionId();
                GetPolicyVersionResponse policyVersionResponse = iam.getPolicyVersion(GetPolicyVersionRequest.builder().policyArn(policy.policyArn()).versionId(policyVersionId).build());

                String policyDocument = policyVersionResponse.policyVersion().document();

                String decodedPolicyDocument = URLDecoder.decode(policyDocument, StandardCharsets.UTF_8.toString());
                System.out.println("decodedPolicyDocument  " + decodedPolicyDocument);

                // Check if the policy contains S3 access permissions
                if (decodedPolicyDocument.contains("s3:PutObject*") && decodedPolicyDocument.contains("s3:GetObject*") && decodedPolicyDocument.contains("s3:DeleteObject*")) {
                    s3Access = true;
                    break;
                }
            }
        }

        Assert.assertTrue("The application should have access to the S3 bucket via an IAM role", s3Access);
    }

}
