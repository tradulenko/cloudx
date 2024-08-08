package com.aws.cloudx_tasks.SNS_SQS_task;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class CXQA_SNSSQS_02_SNS_topic_requirements extends AbstractTest{
    private static final Logger logger = LoggerFactory.getLogger(CXQA_SNSSQS_02_SNS_topic_requirements.class);

    @Test
    public void snsTopicRequirements(){
        // Отримання та перевірка списку топіків
//        verifyTopicNames(snsClient);
        verifyTopicNamesAndAttributes(snsClient);
    }
    private static void verifyTopicNamesAndAttributes(SnsClient snsClient) {
        String prefix = "cloudximage-TopicSNSTopic";
        ListTopicsResponse listTopicsResponse = snsClient.listTopics(ListTopicsRequest.builder().build());
        List<String> topicArns = listTopicsResponse.topics().stream()
                .map(Topic::topicArn)
                .collect(Collectors.toList());

        Assert.assertTrue("There should be at least one topic with the specified prefix.",
                topicArns.stream().anyMatch(arn -> arn.contains(prefix)));

        for (String topicArn : topicArns) {
            boolean isFifoTopic = topicArn.endsWith(".fifo");
            Assert.assertFalse("The topic should not be a FIFO topic.", isFifoTopic);
            logger.info("Verified: Topic {} is not a FIFO topic.", topicArn);

            if (topicArn.contains(prefix)) {
                checkAttributesAndTags(snsClient, topicArn);
            }
        }
    }

    private static void checkAttributesAndTags(SnsClient snsClient, String topicArn) {
        GetTopicAttributesResponse attrs = snsClient.getTopicAttributes(
                GetTopicAttributesRequest.builder()
                        .topicArn(topicArn)
                        .build());

        // Checking encryption
        String kmsKeyId = attrs.attributes().get("KmsMasterKeyId");
        Assert.assertNull("Encryption should be disabled or use default AWS SNS key.", kmsKeyId);
        logger.info("Verified: Encryption is disabled for topic {}.", topicArn);


        // Checking tags
        ListTagsForResourceResponse tagsResponse = snsClient.listTagsForResource(
                ListTagsForResourceRequest.builder()
                        .resourceArn(topicArn)
                        .build());

        boolean tagFound = tagsResponse.tags().stream()
                .anyMatch(tag -> "cloudx".equals(tag.key()) && "qa".equals(tag.value()));

        Assert.assertTrue("Tag 'cloudx: qa' should exist.", tagFound);
        logger.info("Verified: Tag 'cloudx: qa' exists on topic {}.", topicArn);

    }
}