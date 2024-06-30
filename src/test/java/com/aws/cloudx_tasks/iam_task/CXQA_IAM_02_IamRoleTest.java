package com.aws.cloudx_tasks.iam_task;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListRolesResponse;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@RunWith(Parameterized.class)
public class CXQA_IAM_02_IamRoleTest extends AbstractTest{


    @Parameterized.Parameter
    public String roleNameExpected;

    @Parameterized.Parameter(1)
    public String policiesExpected;

    @Parameterized.Parameters(name = "{index}: params={0},{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"FullAccessRoleEC2", "FullAccessPolicyEC2"},
                {"FullAccessRoleS3","FullAccessPolicyS3"},
                {"ReadAccessRoleS3","ReadAccessPolicyS3"}
        });
    }

    @Test
    public void TC_iam_check_role_policies(){

        ListRolesResponse listRolesResponse = iam.listRoles();
        boolean hasRole = listRolesResponse.roles().stream()
                .anyMatch(role -> role.roleName().equals(roleNameExpected));
        Assert.assertTrue(roleNameExpected + " role is not found", hasRole);

        List<String> policiesNamesList = iam.listAttachedRolePolicies(ListAttachedRolePoliciesRequest.builder().roleName(roleNameExpected).build()).attachedPolicies()
                .stream().map(a -> a.policyName()).toList();

        // Check that only one policy for role
        Assert.assertEquals("Number of policies. Actual list is " + policiesNamesList, 1, policiesNamesList.size());

        Assert.assertEquals("Policy for role " + roleNameExpected, policiesExpected, policiesNamesList.get(0));

    }

}
