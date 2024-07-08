package com.aws.cloudx_tasks.ec2_task;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@RunWith(Parameterized.class)
public class CXQA_EC2_02_check_instance_configuration extends AbstractTest {

    final int expectedVolumeSize = 8;
    final String instanceOSExpected = "Amazon Linux 2";

    @Parameterized.Parameter
    public String ec2NameExpected;

    @Parameterized.Parameter(1)
    public String instanceTypeExpected;

    @Parameterized.Parameter(2)
    public String tagValueExpected;

    @Parameterized.Parameter(3)
    public boolean shouldHavePublicIPExpected;

    @Parameterized.Parameters(name = "{index}: params={0},{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"cloudxinfo/PrivateInstance/Instance", "t2.micro", "cloudx:qa", false},
                {"cloudxinfo/PublicInstance/Instance", "t2.micro", "cloudx:qa", true}
        });
    }

    @Test
    public void checkEC2types() {
        DescribeInstancesResponse response = ec2.describeInstances();

        Optional<Instance> actualInstance = response.reservations().stream()
                .flatMap(reservation -> reservation.instances().stream())
                .filter(instance -> instance.tags().stream().anyMatch(tag ->
                        tag.key().equals("Name") && tag.value().equals(ec2NameExpected)))
                .findFirst();

        if (actualInstance.isPresent()) {
            System.out.println("Found the expected instance: " + actualInstance.get().instanceId());
        } else {
            Assert.fail("The expected instance was not found. ExpectedInstanceName is " + ec2NameExpected);
        }


        Assert.assertEquals("Instance type", instanceTypeExpected, actualInstance.get().instanceType().toString());

        Assert.assertEquals("", tagValueExpected.split(":")[1], getTag(actualInstance.get()).value());

        if (shouldHavePublicIPExpected) {
            Assert.assertNotNull(ec2NameExpected + " should have public IP ", actualInstance.get().publicIpAddress());
        } else {
            Assert.assertNull(ec2NameExpected + " should not have public IP ", actualInstance.get().publicIpAddress());
        }


        // check volume
        Instance instance = actualInstance.get();
        String rootVolumeId = instance.blockDeviceMappings().stream()
                .filter(bdm -> bdm.deviceName().equals(instance.rootDeviceName()))
                .findFirst()
                .map(bdm -> bdm.ebs().volumeId())
                .orElseThrow();

        DescribeVolumesResponse describeVolumesResponse = ec2.describeVolumes(DescribeVolumesRequest.builder()
                .volumeIds(rootVolumeId)
                .build());

        int rootVolumeSize = describeVolumesResponse.volumes().get(0).size();

        Assert.assertEquals(rootVolumeId + " Root block device size for instance " + ec2NameExpected, expectedVolumeSize, rootVolumeSize);


        //  Instance OS: Amazon Linux 2
        String amiId = instance.imageId();
        DescribeImagesResponse describeImagesResponse = ec2.describeImages(DescribeImagesRequest.builder().imageIds(amiId).build());
        Image image = describeImagesResponse.images().get(0);
        String description = image.description();
        System.out.println(description);

        Assert.assertTrue("OS should be " + instanceOSExpected + " but description was " + description
                , description.contains(instanceOSExpected));


    }

    private Tag getTag(Instance instance) {
        String tagName = tagValueExpected.split(":")[0];
        Optional<Tag> instanceTag = instance.tags().stream()
                .filter(tag -> tag.key().equals(tagName))
                .findFirst();
        if (instanceTag.isPresent()) {
            System.out.println("Instance has the correct tag.");
        } else {
            Assert.fail("Instance does not have the correct tag.");
        }
        return instanceTag.get();
    }
}
