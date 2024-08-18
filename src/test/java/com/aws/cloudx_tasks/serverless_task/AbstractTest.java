package com.aws.cloudx_tasks.serverless_task;

import org.junit.After;
import org.junit.Before;


import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.applicationautoscaling.ApplicationAutoScalingClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class AbstractTest {
//    cloudxserverless.AppInstanceInstanceId = i-08e368ed7bf2dfe66
//    cloudxserverless.AppInstanceInstanceRoleARN = arn:aws:iam::211125590392:role/cloudxserverless-AppInstanceInstanceRole67BDC071-2wGSLtsqFLqe
//    cloudxserverless.AppInstancePrivateDns = ip-10-0-0-239.ec2.internal
//    cloudxserverless.AppInstancePrivateIp = 10.0.0.239
//    cloudxserverless.AppInstancePublicDns = ec2-3-230-3-119.compute-1.amazonaws.com
//    cloudxserverless.AppInstancePublicIp = 3.230.3.119
//    cloudxserverless.AppInstanceSecurityGroupId = sg-02af9a0dfeedc751a
//    cloudxserverless.EventHandlerLambdaName = cloudxserverless-EventHandlerLambdaECEF2D13-qzee0eR3JgGf
//    cloudxserverless.EventHandlerLambdaRoleARN = arn:aws:iam::211125590392:role/cloudxserverless-EventHandlerLambdaRoleA85151C3-xlyI5hVd0pvj
//    cloudxserverless.ImageBucketArn = arn:aws:s3:::cloudxserverless-imagestorebucketf57d958e-psburb03gb5u
//    cloudxserverless.ImageBucketName = cloudxserverless-imagestorebucketf57d958e-psburb03gb5u
//    cloudxserverlearn:aws:cloudformation:us-east-1:211125590392:stack/cloudxserverless/67058820-5d74-11ef-8d2f-0affd06cd743
//    ss.ImageBucketPolicyARN = arn:aws:iam::211125590392:policy/cloudxserverless-ImageStoreBucketPolicy31DC664C-vS6rF4d9bZ1a
//    cloudxserverless.KeyId = key-04e5b975e611a0cfa
//    cloudxserverless.PrivateSubnetIds = subnet-017da53278041649d,subnet-0e0e928d632f10db0
//    cloudxserverless.PublicSubnetIds = subnet-095c45f59ea77623a,subnet-05ca5a9d7a6cd2006
//    cloudxserverless.QueueArn = arn:aws:sqs:us-east-1:211125590392:cloudxserverless-QueueSQSQueueE7532512-9IeBizkYMD2s
//    cloudxserverless.QueueUrl = https://sqs.us-east-1.amazonaws.com/211125590392/cloudxserverless-QueueSQSQueueE7532512-9IeBizkYMD2s
//    cloudxserverless.TableName = cloudxserverless-DatabaseImagesTable3098F792-1MGHTCYRP6RKO
//    cloudxserverless.TopicArn = arn:aws:sns:us-east-1:211125590392:cloudxserverless-TopicSNSTopic086466D7-rGHKWLnUfQpn
//    cloudxserverless.TopicPublishPolicyArn = arn:aws:iam::211125590392:policy/cloudxserverless-TopicPublishPolicy0C1B6C46-KnWagSeYwrhk
//    cloudxserverless.TopicSubscriptionPolicyArn = arn:aws:iam::211125590392:policy/cloudxserverless-TopicSubscriptionPolicyCA0E2B56-DUWQhwDPJV4G
//    cloudxserverless.TrailArn = arn:aws:cloudtrail:us-east-1:211125590392:trail/cloudxserverless-Trail76E5F934-QYPVdLAVpffL
//    cloudxserverless.TrailBucketArn = arn:aws:s3:::cloudxserverless-trailtrailbucket619c0c04-qvvje1of5gwi
//    cloudxserverless.TrailBucketName = cloudxserverless-trailtrailbucket619c0c04-qvvje1of5gwi
//    cloudxserverless.VpcId = vpc-097825b80d36cac56
    protected String AppInstancePublicIp = "3.230.3.119";
    protected String EventHandlerLambdaName = "cloudxserverless-EventHandlerLambdaECEF2D13-qzee0eR3JgGf";
    protected String QueueUrl = "https://sqs.us-east-1.amazonaws.com/211125590392/cloudxserverless-QueueSQSQueueE7532512-9IeBizkYMD2s";
    protected String TableName = "cloudxserverless-DatabaseImagesTable3098F792-1MGHTCYRP6RKO";

    protected static DynamoDbClient dynamoDBClient;
    protected static ApplicationAutoScalingClient autoScalingClient;
    @Before
    public void setup() {
        dynamoDBClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1)  // Update to your region
                .build();

        autoScalingClient = ApplicationAutoScalingClient.builder()
                .region(Region.US_EAST_1)  // Update to your region
                .build();
    }

    @After
    public void tearDown() {
    }
}
