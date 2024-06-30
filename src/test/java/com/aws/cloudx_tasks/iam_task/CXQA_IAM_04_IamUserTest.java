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
public class CXQA_IAM_04_IamUserTest extends AbstractTest {


    @Parameterized.Parameter
    public String userNameExpected;

    @Parameterized.Parameter(1)
    public String groupNameExpected;

    @Parameterized.Parameters(name = "{index}: params={0},{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"FullAccessUserEC2", "FullAccessGroupEC2"},
                {"FullAccessUserS3", "FullAccessGroupS3"},
                {"ReadAccessUserS3", "ReadAccessGroupS3"}
        });
    }

    @Test
    public void TC_iam_check_user_group() {

        List<Group> groups = iam.listGroupsForUser(
                ListGroupsForUserRequest.builder()
                        .userName(userNameExpected)
                        .build()
        ).groups();

        System.out.println(groups);

        Assert.assertEquals("Number of groups. Actual list is " + groups, 1, groups.size());
        Assert.assertEquals("Group for user " + userNameExpected, groupNameExpected, groups.get(0).groupName());

    }

}
