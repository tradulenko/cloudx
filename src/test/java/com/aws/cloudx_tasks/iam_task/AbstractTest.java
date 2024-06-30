package com.aws.cloudx_tasks.iam_task;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;

public class AbstractTest {

    private static final Region REGION = Region.EU_NORTH_1;

    protected static IamClient iam;

    @BeforeClass
    public static void initClient() {
        iam = IamClient.builder()
                .region(Region.AWS_GLOBAL)
//                .region(REGION)
                .build();
    }

    @AfterClass
    public static void tearDown(){
        iam.close();
    }

}
