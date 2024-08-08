package com.aws.cloudx_tasks.SNS_SQS_task;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

public class CXQA_SNSSQS_03_SQS_queue_requirements {
    private static final Logger logger = LoggerFactory.getLogger(CXQA_SNSSQS_03_SQS_queue_requirements.class);


    @Test
    public void verifyQ() {
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .build();

        try {
            verifyQueueAttributesAndTags(sqsClient);
        } finally {
            sqsClient.close();
        }

    }

    private void verifyQueueAttributesAndTags(SqsClient sqsClient) {

        String queueNamePattern = "cloudximage-QueueSQSQueue";

        ListQueuesResponse listQueuesResponse = sqsClient.listQueues();
        List<String> queueUrls = listQueuesResponse.queueUrls();

        // Assert that there is at least one queue with the required pattern
        Assert.assertTrue("There should be at least one queue with the specified name pattern.",
                queueUrls.stream().anyMatch(url -> url.contains(queueNamePattern)));

        for (String queueUrl : queueUrls) {
            if (queueUrl.contains(queueNamePattern)) {
                GetQueueAttributesResponse attrs = sqsClient.getQueueAttributes(
                        GetQueueAttributesRequest.builder()
                                .queueUrl(queueUrl)
                                .attributeNames(QueueAttributeName.ALL)
                                .build());

                // Check Queue Type
                String fifoQueue = attrs.attributes().get(QueueAttributeName.FIFO_QUEUE.toString());
                Assert.assertNull("The queue must be a standard queue, not FIFO.", fifoQueue);

                // Check Tags
                ListQueueTagsResponse tagsResponse = sqsClient.listQueueTags(ListQueueTagsRequest.builder().queueUrl(queueUrl).build());
                String tagValue = tagsResponse.tags().get("cloudx");
                Assert.assertEquals("Tag 'cloudx' should be 'qa'", "qa", tagValue);

                // Check Dead-letter Queue
                String redrivePolicy = attrs.attributes().get(QueueAttributeName.REDRIVE_POLICY.toString());
                Assert.assertNull("There should be no dead-letter queue configured.", redrivePolicy);

                logger.info("Verified: Queue {} meets all requirements.", queueUrl);
            }
        }
    }
}