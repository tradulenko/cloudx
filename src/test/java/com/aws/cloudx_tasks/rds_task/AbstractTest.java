package com.aws.cloudx_tasks.rds_task;

import org.junit.Before;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;

public class AbstractTest {

    protected RdsClient rdsClient;

    @Before
    public void init(){
        rdsClient = RdsClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }





}
