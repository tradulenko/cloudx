package com.aws.cloudx_tasks.s3_task;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.List;

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



    protected List<Instance> getListRunningInstances (){
        DescribeInstancesResponse response = ec2.describeInstances();

        List<Instance> instanceList = response.reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .filter(instance -> instance.state().code() == 16)
                .toList();

        if (instanceList.size() == 1) {
            System.out.println("Found the expected instance: " + instanceList.get(0).instanceId());
        } else {
            Assert.fail("The expected instance was not found.  List of instances  " + instanceList);
        }

        return instanceList;
    }
}
