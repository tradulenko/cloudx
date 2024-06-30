package com.aws.cloudx_tasks.suits;


import com.aws.cloudx_tasks.iam_task.CXQA_IAM_03_IamGroupsTest;
import com.aws.cloudx_tasks.iam_task.CXQA_IAM_01_IamPoliciesTests;
import com.aws.cloudx_tasks.iam_task.CXQA_IAM_02_IamRoleTest;
import com.aws.cloudx_tasks.iam_task.CXQA_IAM_04_IamUserTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CXQA_IAM_03_IamGroupsTest.class,
        CXQA_IAM_01_IamPoliciesTests.class,
        CXQA_IAM_02_IamRoleTest.class,
        CXQA_IAM_04_IamUserTest.class
})
public class RegressionSuite {
}
