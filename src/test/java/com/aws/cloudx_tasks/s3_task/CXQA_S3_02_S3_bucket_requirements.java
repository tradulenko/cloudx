package com.aws.cloudx_tasks.s3_task;

import org.junit.Assert;
import org.junit.Test;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

public class CXQA_S3_02_S3_bucket_requirements {
    final String symbols = "-".repeat(10);

    @Test
    public void checkS3bucketReq() {
        S3Client s3 = S3Client.create();

//Name: cloudximage-imagestorebucket{unique id}
        System.out.println(symbols + " Name: cloudximage-imagestorebucket{unique id}" + symbols);
        boolean bucketExists = false;
        String bucketName = "cloudximage-imagestorebucket";
        String actualBucketName = "";

        ListBucketsResponse listBucketsResponse = s3.listBuckets();
        for (Bucket bucket : listBucketsResponse.buckets()) {
            System.out.println("Bucket name " + bucket.name());
            if (bucket.name().contains(bucketName)) {
                actualBucketName = bucket.name();
                bucketExists = true;
                break;
            }
        }

        Assert.assertTrue("Bucket with name: " + bucketName + " does not exist", bucketExists);

//Tags: cloudx: qa
        System.out.println(symbols + " Tags: cloudx: qa " + symbols);
        GetBucketTaggingResponse getBucketTaggingResponse = s3.getBucketTagging(GetBucketTaggingRequest.builder().bucket(actualBucketName).build());
        List<Tag> tags = getBucketTaggingResponse.tagSet();

        boolean tagExists = false;
        String requiredKey = "cloudx";
        String requiredValue = "qa";

        for (Tag tag : tags) {
            if (tag.key().equals(requiredKey) && tag.value().equals(requiredValue)) {
                tagExists = true;
                break;
            }
        }

        Assert.assertTrue("Required tag does not exist in bucket: " + actualBucketName + " tag " + requiredValue, tagExists);

//Encryption type: SSE-S3
        System.out.println(symbols + " Encryption type: SSE-S3 " + symbols);
        GetBucketEncryptionResponse getBucketEncryptionResponse = s3.getBucketEncryption(GetBucketEncryptionRequest.builder().bucket(actualBucketName).build());
        ServerSideEncryptionConfiguration serverSideEncryptionConfiguration = getBucketEncryptionResponse.serverSideEncryptionConfiguration();

        boolean encryptionTypeIsCorrect = false;

        for (ServerSideEncryptionRule rule : serverSideEncryptionConfiguration.rules()) {
            if (rule.applyServerSideEncryptionByDefault() != null) {
                String encryptionType = rule.applyServerSideEncryptionByDefault().sseAlgorithmAsString();
                if (encryptionType.equals("AES256")) {
                    encryptionTypeIsCorrect = true;
                    break;
                }
            }
        }

        Assert.assertTrue("Bucket does not use SSE-S3 encryption", encryptionTypeIsCorrect);

// Versioning: disabled
        System.out.println(symbols + " Versioning: disabled " + symbols);
        GetBucketVersioningResponse getBucketVersioningResponse = s3.getBucketVersioning(GetBucketVersioningRequest.builder().bucket(actualBucketName).build());
        BucketVersioningStatus versioningStatus = getBucketVersioningResponse.status();
        System.out.println("VersioningStatus is " + versioningStatus);

        Assert.assertNull("Versioning should be disabled for the bucket.", versioningStatus);

//Public access: no
        System.out.println(symbols + " Public access: no " + symbols);
        GetPublicAccessBlockResponse getPublicAccessBlockResponse = s3.getPublicAccessBlock(GetPublicAccessBlockRequest.builder().bucket(actualBucketName).build());
        PublicAccessBlockConfiguration publicAccessBlockConfiguration = getPublicAccessBlockResponse.publicAccessBlockConfiguration();

        Assert.assertTrue("Bucket should have public ACL access blocked", publicAccessBlockConfiguration.blockPublicAcls());
        Assert.assertTrue("Bucket should have public policy access blocked", publicAccessBlockConfiguration.blockPublicPolicy());
        Assert.assertTrue("Bucket should ignore public ACLs", publicAccessBlockConfiguration.ignorePublicAcls());
        Assert.assertTrue("Bucket should have public access restricted", publicAccessBlockConfiguration.restrictPublicBuckets());

    }
}
