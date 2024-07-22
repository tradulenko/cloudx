package com.aws.cloudx_tasks.s3_task;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

public class AbstractTest {

    private static final Region REGION = Region.US_EAST_1;

    protected static Ec2Client ec2;

    @BeforeClass
    public static void initClient() {
        ec2 = Ec2Client.builder()
                .region(REGION)
                .build();
    }

    @AfterClass
    public static void tearDown(){
        ec2.close();
    }

}
