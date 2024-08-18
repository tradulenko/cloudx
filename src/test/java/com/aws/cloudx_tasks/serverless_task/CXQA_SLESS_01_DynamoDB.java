package com.aws.cloudx_tasks.serverless_task;

import org.junit.Test;

import software.amazon.awssdk.services.applicationautoscaling.model.DescribeScalingPoliciesRequest;
import software.amazon.awssdk.services.applicationautoscaling.model.DescribeScalingPoliciesResponse;
import software.amazon.awssdk.services.applicationautoscaling.model.ScalingPolicy;
import software.amazon.awssdk.services.dynamodb.model.*;

import static org.junit.Assert.*;

public class CXQA_SLESS_01_DynamoDB extends AbstractTest{
    @Test
    public void testDynamoDBTableConfiguration() {
        // Check table configuration
        DescribeTableResponse describeTableResponse = dynamoDBClient.describeTable(DescribeTableRequest.builder()
                .tableName(TableName)
                .build());

        TableDescription table = describeTableResponse.table();

        // Check table name
        assertEquals("Table name should match.", TableName, table.tableName());

        // Check for global secondary indexes
        assertTrue("There should be no global secondary indexes.",
                table.globalSecondaryIndexes() == null || table.globalSecondaryIndexes().isEmpty());

        // Check provisioned throughput settings
        ProvisionedThroughputDescription throughputDescription = table.provisionedThroughput();
        assertEquals("Provisioned Read Capacity Units should be 5.", 5L, throughputDescription.readCapacityUnits().longValue());


        // Check Auto Scaling settings for reads
        DescribeScalingPoliciesResponse readScalingPolicy = autoScalingClient.describeScalingPolicies(DescribeScalingPoliciesRequest.builder()
                .serviceNamespace("dynamodb")
                .resourceId("table/" + TableName)
                .scalableDimension("dynamodb:table:ReadCapacityUnits")
                .build());
        assertTrue("Autoscaling for reads should be off.", readScalingPolicy.scalingPolicies().isEmpty());

        // Check Auto Scaling settings for writes
        DescribeScalingPoliciesResponse writeScalingPolicy = autoScalingClient.describeScalingPolicies(DescribeScalingPoliciesRequest.builder()
                .serviceNamespace("dynamodb")
                .resourceId("table/" + TableName)
                .scalableDimension("dynamodb:table:WriteCapacityUnits")
                .build());
        assertFalse("Autoscaling for writes should be on.", writeScalingPolicy.scalingPolicies().isEmpty());

        // Optionally, verify the specific settings of the auto-scaling policy
        if (!writeScalingPolicy.scalingPolicies().isEmpty()) {
            ScalingPolicy policy = writeScalingPolicy.scalingPolicies().get(0);
            // Example check - verify the policy name, bounds, or other attributes
            assertNotNull("Write capacity auto-scaling policy should exist.", policy);
        }

        // Check Time to Live (TTL) setting
        DescribeTimeToLiveResponse ttlResponse = dynamoDBClient.describeTimeToLive(DescribeTimeToLiveRequest.builder()
                .tableName(TableName)
                .build());
        assertEquals("Time to Live should be disabled.", TimeToLiveStatus.DISABLED, ttlResponse.timeToLiveDescription().timeToLiveStatus());

        // Check tags
        ListTagsOfResourceResponse tagsResponse = dynamoDBClient.listTagsOfResource(ListTagsOfResourceRequest.builder()
                .resourceArn(table.tableArn())
                .build());
        assertTrue("Tag cloudx: qa must be present", tagsResponse.tags().stream()
                .anyMatch(tag -> tag.key().equals("cloudx") && tag.value().equals("qa")));
    }
}
