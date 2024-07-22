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
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class CXQA_S3_03_04_05_06_work_with_file extends AbstractTest {
    String publicIpAddress;

    RequestSpecification requestSpecification;

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

    }


    @Test
    public void workWithImage() throws IOException {
// check empty bucket
        RestAssured.given()
                .spec(requestSpecification)
                .basePath("/api/image")
                .get()
                .then()
                .statusCode(200)
                .log().all()
                .body("", Matchers.emptyIterable())
        ;

// CXQA-S3-03 upload image
        System.out.println("CXQA-S3-03 upload image");
        Response response = RestAssured.given()
                .spec(requestSpecification)
                .basePath("/api/image")

                .multiPart("upfile", new File("src/main/resources/images/maxresdefault.jpg"))
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
        System.out.println("The id is: " + id);


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
        ;


// CXQA-S3-04 Download file
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
        FileOutputStream fos = new FileOutputStream(".\\target\\files\\image.jpg"); // вкажіть шлях, де ви бажаєте зберегти файл
        fos.write(fileContent);
        fos.close();


        // CXQA-S3-06 delete image
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


// check empty bucket
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
