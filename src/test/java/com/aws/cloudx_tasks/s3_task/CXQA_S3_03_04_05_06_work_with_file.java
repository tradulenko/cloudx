package com.aws.cloudx_tasks.s3_task;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class CXQA_S3_03_04_05_06_work_with_file extends AbstractTest {
    String publicIpAddress;
    final String symbols = "-".repeat(10);

    RequestSpecification requestSpecification;
    String actualBucketName = "";

    S3Client s3;

    @Before
    public void setup() {

        File dir = new File(".\\target\\files");
        dir.mkdirs();
        for (File file : dir.listFiles()) {
            file.delete();
            System.out.println(file + " file was  deleted");
        }
        System.out.println("Files were deleted ");

        List<Instance> instanceList = getListRunningInstances();

        Instance actualInstance = instanceList.get(0);
        publicIpAddress = actualInstance.publicIpAddress();

        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://ec2-" + publicIpAddress.replace(".", "-") + ".compute-1.amazonaws.com")
                .log(LogDetail.ALL)
                .build();


         s3 = S3Client.create();

//Name: cloudximage-imagestorebucket{unique id}
        System.out.println(symbols + " Name: cloudximage-imagestorebucket{unique id}" + symbols);
        boolean bucketExists = false;
        String bucketName = "cloudximage-imagestorebucket";


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
    }


    @Test
    public void workWithImage() throws IOException {
// check empty bucket
        checkEmptyBucket();

// CXQA-S3-03 upload image
        int id = uploadFileToBucket("src/main/resources/images/maxresdefault.jpg");
        System.out.println("The id is: " + id);


// CXQA-S3-03 upload image
        int id_second = uploadFileToBucket("src/main/resources/images/maxresdefault.jpg");
        System.out.println("The id is: " + id_second);

// CXQA-S3-05: View a list of uploaded images
        System.out.println("CXQA-S3-05: View a list of uploaded images");
        RestAssured.given()
                .spec(requestSpecification)
                .basePath("/api/image")
                .get()
                .then()
                .statusCode(200)
                .log().all()
                .body("id[0]", equalTo(id))
                .body("id[1]", equalTo(id_second))
        ;


// check number of files in bucket
        int imageCount = 0;
        ArrayList listOfFiles = new ArrayList<>();

        ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder()
                .bucket(actualBucketName)
                .maxKeys(10)
                .build();

        ListObjectsV2Response listObjResponse = s3.listObjectsV2(listObjectsReqManual);

        for (S3Object content : listObjResponse.contents()) {
            System.out.println(content.key());
            listOfFiles.add(content.key());
            if (content.key().contains("maxresdefault.jpg")) {
                imageCount++;
            }
        }

        Assert.assertEquals("number of images that contains maxresdefault.jpg in name. Actual list " + listOfFiles ,  imageCount, 2);

// CXQA-S3-04 Download file
        downloadFile(id);
        downloadFile(id_second);


        // CXQA-S3-06 delete images
        deleteFileWithId(id);
        deleteFileWithId(id_second);

        // check empty bucket
        checkEmptyBucket();

    }

    private void downloadFile(int id) throws IOException {
        System.out.println("CXQA-S3-04 Download file");
        byte[] fileContent = RestAssured.given()
                .spec(requestSpecification)
                .basePath("/api/image/file/" + id)
                .accept(ContentType.ANY)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .asByteArray();

        File dir = new File(".\\target\\files");
        dir.mkdirs();
        System.out.println("Directory is created? " + dir.exists());
        FileOutputStream fos = new FileOutputStream(".\\target\\files\\image-"+ id +".jpg"); // вкажіть шлях, де ви бажаєте зберегти файл
        fos.write(fileContent);
        fos.close();
    }

    private String deleteFileWithId(int id) {
        System.out.println("CXQA-S3-06 delete image");
        String responseDelete = RestAssured.given()
                .spec(requestSpecification)
                .accept("application/json")
                .basePath("/api/image/" + id)
                .delete()
                .then()
                .statusCode(200)
                .log().all()
                .extract().asString().trim().replace("\"", "");
        Assert.assertEquals("Message in response Delete ", "Image is deleted", responseDelete);
        return responseDelete;
    }

    private int uploadFileToBucket(String pathToFile) {
        System.out.println("CXQA-S3-03 upload image");
        Response response = RestAssured.given()
                .spec(requestSpecification)
                .basePath("/api/image")

                .multiPart("upfile", new File(pathToFile))
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(200)
                .log().all()
                .extract().response();

// remember id from response
        JsonPath jsonPath = new JsonPath(response.asString());
        int id = jsonPath.getInt("id");
        return id;
    }

    private void checkEmptyBucket() {
        RestAssured.given()
                .spec(requestSpecification)
                .basePath("/api/image")
                .get()
                .then()
                .statusCode(200)
                .log().all()
                .body("", Matchers.emptyIterable())
        ;
    }

}
