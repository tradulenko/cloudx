package com.aws.cloudx_tasks.iam_task;

import com.aws.cloudx_tasks.iam_task.dto.PolicyDocumentDTO;
import com.aws.cloudx_tasks.iam_task.dto.PolicyStatementDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.iam.model.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@RunWith(Parameterized.class)
public class CXQA_IAM_01_IamPoliciesTests extends AbstractTest {
    @Parameterized.Parameter
    public String policyNameExpected;

    @Parameterized.Parameter(1)
    public String actionsExpected;

    @Parameterized.Parameter(2)
    public String resourcesExpected;

    @Parameterized.Parameter(3)
    public String effectExpected;

    @Parameterized.Parameters(name = "{index}: params={0},{1},{2},{3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"FullAccessPolicyEC2", "ec2:*", "*", "Allow"},
                {"FullAccessPolicyS3", "s3:*", "*", "Allow"},
                {"ReadAccessPolicyS3", "s3:Describe*,s3:Get*,s3:List*", "*", "Allow"}
        });
    }


    @Test
    public void TC_iam_check_policies() throws JsonProcessingException, UnsupportedEncodingException {
        Optional<Policy> actualPolicy = iam.listPolicies().policies()
                .stream()
                .filter(policy -> policyNameExpected.equals(policy.policyName())).findFirst();

        //Check policy exists
        Assert.assertTrue(policyNameExpected + "  was not found", actualPolicy.isPresent());
        String actualArn = actualPolicy.get().arn();


//        System.out.println("Actual Policy arn" + actualArn);

        GetPolicyVersionResponse getPolicyVersionResponse = iam.getPolicyVersion(GetPolicyVersionRequest.builder()
                .policyArn(actualArn)
                .versionId(actualPolicy.get().defaultVersionId())
                .build());

//        System.out.println(getPolicyVersionResponse.policyVersion());
        String policyDocument = getPolicyVersionResponse.policyVersion().document();
        String decodedPolicyDocument = URLDecoder.decode(policyDocument, StandardCharsets.UTF_8.toString());

        ObjectMapper mapper = new ObjectMapper();
        PolicyDocumentDTO policy = mapper.readValue(decodedPolicyDocument, PolicyDocumentDTO.class);

        Assert.assertEquals("Should be one statement." , 1, policy.getStatement().size());

        PolicyStatementDTO firstStatement = policy.getStatement().get(0);

        String actualActions = getActionsAsString(firstStatement);

        System.out.println(policyNameExpected + ": Action " + actualActions);
        System.out.println(policyNameExpected + ": Resource " + firstStatement.getResource());
        System.out.println(policyNameExpected + ": Effect " + firstStatement.getEffect());

        Assert.assertEquals(policyNameExpected + ": Effect ", effectExpected, firstStatement.getEffect());
        Assert.assertEquals(policyNameExpected + ": Resource ", resourcesExpected, firstStatement.getResource());
        Assert.assertEquals(policyNameExpected + ": Action ", actionsExpected, actualActions);


    }

    private static String getActionsAsString(PolicyStatementDTO firstStatement) {
        return firstStatement.getAction().toString()
                .replace(" ", "")
                .replace("[", "")
                .replace("]", "");
    }


}
