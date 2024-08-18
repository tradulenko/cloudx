package com.aws.cloudx_tasks.serverless_task;



import org.junit.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CXQA_SLESS_07_AWS_Lambda_requirements extends AbstractTest{
    private final SqsClient sqsClient = SqsClient.builder().region(Region.US_EAST_1).build();
    private final CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder().region(Region.US_EAST_1).build();
    private final LambdaClient lambdaClient = LambdaClient.builder().region(Region.US_EAST_1).build();

    @Test
    public void testLambdaTriggerAndLogging() throws InterruptedException {
        // Запуск повідомлення в SQS

        String messageBody = "Test message";
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(QueueUrl)
                .messageBody(messageBody)
                .build());

        // Чекати, щоб Lambda могла обробити повідомлення
        TimeUnit.SECONDS.sleep(10);

        // Перевірити, що записи є в CloudWatch Logs
        long startTime = Instant.now().minusSeconds(20).toEpochMilli();
        long endTime = Instant.now().toEpochMilli();

        FilterLogEventsResponse response = logsClient.filterLogEvents(FilterLogEventsRequest.builder()
                .logGroupName("/aws/lambda/" + EventHandlerLambdaName)
                .startTime(startTime)
                .endTime(endTime)
                .filterPattern("Test message")
                .build());

        assertTrue("Log events containing 'Test message' should exist", response.events().size() > 0);

        GetFunctionConfigurationResponse functionConfig = lambdaClient.getFunctionConfiguration(
                GetFunctionConfigurationRequest.builder()
                        .functionName(EventHandlerLambdaName)
                        .build()
        );
        System.out.println("DEBUG: Lambda function configuration: " + functionConfig);

        assertEquals("Memory should be 128 MB", 128, functionConfig.memorySize().intValue());
        assertEquals("Timeout should be 3 sec", 3, functionConfig.timeout().intValue());

        GetFunctionResponse functionResponse = lambdaClient.getFunction(GetFunctionRequest.builder()
                .functionName(EventHandlerLambdaName)
                .build());
        String lambdaArn = functionResponse.configuration().functionArn();

        // Запит для отримання тегів
        ListTagsResponse tagsResponse = lambdaClient.listTags(ListTagsRequest.builder()
                .resource(lambdaArn)
                .build());

        // Перевірка тега
        assertTrue("Tag 'cloudx:qa' should exist", tagsResponse.tags().containsKey("cloudx") &&
                tagsResponse.tags().containsValue("qa"));
    }
}