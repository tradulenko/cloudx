package com.aws.cloudx_tasks.iam_task;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.iam.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@RunWith(Parameterized.class)
public class CXQA_IAM_03_IamGroupsTest extends AbstractTest {


    @Parameterized.Parameter
    public String groupNameExpected;

    @Parameterized.Parameter(1)
    public String policiesExpected;

    @Parameterized.Parameters(name = "{index}: params={0},{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"FullAccessGroupEC2", "FullAccessPolicyEC2"},
                {"FullAccessGroupS3","FullAccessPolicyS3"},
                {"ReadAccessGroupS3", "ReadAccessPolicyS3"}
        });
    }

    @Test
    public void TC_iam_check_group_policies() {

        ListGroupsResponse listGroupsResponse = iam.listGroups();
        System.out.println(listGroupsResponse.groups());

        List<AttachedPolicy> attachedPolicies = iam.listAttachedGroupPolicies(
                ListAttachedGroupPoliciesRequest.builder()
                        .groupName(groupNameExpected)
                        .build()
        ).attachedPolicies();

        System.out.println(attachedPolicies);

//        // Check that only one policy for group
        Assert.assertEquals("Number of policies. Actual list is " + attachedPolicies, 1, attachedPolicies.size());

        Assert.assertEquals("Policy fo group " + groupNameExpected, policiesExpected, attachedPolicies.get(0).policyName());

    }

}
