package com.aws.cloudx_tasks.rds_task;

import org.junit.Test;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.rds.model.Tag;

import java.util.List;

public class CXQA_RDS_02_RDS_instance_requirements extends AbstractTest {


    @Test
    public void checkRdsInstanceRequirements(){
        DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
        for (DBInstance dbInstance : response.dbInstances()) {
            // Запит на теги ресурсу
            List<Tag> tags = rdsClient.listTagsForResource(ListTagsForResourceRequest.builder()
                    .resourceName(dbInstance.dbInstanceArn())
                    .build()).tagList();

            boolean hasCorrectTag = tags.stream().anyMatch(tag -> tag.key().equals("cloudx") && tag.value().equals("qa"));

            // Перевірки
            assert dbInstance.dbInstanceClass().equals("db.t3.micro") : "Instance type is not 'db.t3.micro'";
            assert !dbInstance.multiAZ() : "Instance is incorrectly configured as Multi-AZ";
            assert dbInstance.allocatedStorage() == 100 : "Storage size is not equal to 100 GiB";
            assert dbInstance.storageType().equals("gp2") : "Storage type is not 'General Purpose SSD (gp2)'";
            assert !dbInstance.storageEncrypted() : "Encryption should not be enabled";
            assert hasCorrectTag : "Instance does not have the correct 'cloudx: qa' tag";
            assert dbInstance.engine().equalsIgnoreCase("mysql") : "Database type is not MySQL";
            assert dbInstance.engineVersion().startsWith("8.0.32") : "Database version is not '8.0.32'";

            System.out.println("Instance ID: " + dbInstance.dbInstanceIdentifier());
            System.out.println("Instance Type: " + dbInstance.dbInstanceClass());
            System.out.println("Multi-AZ enabled: " + dbInstance.multiAZ());
            System.out.println("Storage Size: " + dbInstance.allocatedStorage() + " GiB");
            System.out.println("Storage Type: " + dbInstance.storageType());
            System.out.println("Encryption Enabled: " + dbInstance.storageEncrypted());
            System.out.println("Database Type: " + dbInstance.engine());
            System.out.println("Database Version: " + dbInstance.engineVersion());
            System.out.println("Tags: " + tags);
        }
    }


}
