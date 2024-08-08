package com.aws.cloudx_tasks.SNS_SQS_task;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.sns.SnsClient;

public class AbstractTest {

    private static final Region REGION = Region.US_EAST_1;

    protected static Ec2Client ec2;
    protected static SnsClient snsClient;

    @BeforeClass
    public static void initClient() {
        snsClient = SnsClient.builder()
                .region(REGION)
                .build();
    }

    @AfterClass
    public static void tearDown(){
        snsClient.close();
    }



}
