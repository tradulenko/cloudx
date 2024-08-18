package com.aws.cloudx_tasks.serverless_task;

import dto.ImageMetaData;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CXQA_SLESS_02_table_data extends AbstractTest {


    RequestSpecification requestSpecification;
    final String fileName = "maxresdefault.jpg";

    private ImageMetaData[] imageMetaData;
    String uploadedFileId;

    @Before
    public void setup_test() {
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://ec2-" + AppInstancePublicIp.replace(".", "-") + ".compute-1.amazonaws.com")
                .log(LogDetail.ALL)
                .build();

        uploadedFileId = uploadFileToBucket("src/main/resources/images/" + fileName);
        System.out.println("The id is: " + uploadedFileId);


//        System.out.println("The id 2 is: " + uploadFileToBucket("src/main/resources/images/f-01.jpg"));
//        System.out.println("The id 3 is: " + uploadFileToBucket("src/main/resources/images/f-02.jpg"));
//        System.out.println("The id 4 is: " + uploadFileToBucket("src/main/resources/images/f-03.jpg"));
//        System.out.println("The id 5 is: " + uploadFileToBucket("src/main/resources/images/f-04.jpg"));
//        System.out.println("The id 6 is: " + uploadFileToBucket("src/main/resources/images/f-05.jpg"));
//        System.out.println("The id 7 is: " + uploadFileToBucket("src/main/resources/images/f-06.jpg"));

        imageMetaData = RestAssured.given()
                .spec(requestSpecification)
                .basePath("/api/image")
                .get()
                .then()
                .statusCode(200)
                .log().all()
                .extract().as(ImageMetaData[].class);

        System.out.println("The image metadata is: " + imageMetaData.toString());

    }

    @Test
    public void testImageMetadataStorage() {

        DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(TableName)
                .build();

        DescribeTableResponse response1 = dynamoDBClient.describeTable(request);

        List<KeySchemaElement> keySchema = response1.table().keySchema();
        for (KeySchemaElement keyElem : keySchema) {
            System.out.println("AttributeName: " + keyElem.attributeName() +
                    ", KeyType: " + keyElem.keyType());
        }


        // Отримання запису з таблиці
        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName(TableName)
                .key(Map.of("id", AttributeValue.builder().s(imageMetaData[0].getId()).build()))
                .build();
        GetItemResponse response = dynamoDBClient.getItem(getRequest);

        Map<String, AttributeValue> retrievedItem = response.item();

        // Перевірки метаданих
        assertEquals("Object keys should match", imageMetaData[0].getObjectKey(), retrievedItem.get("object_key").s());
        assertEquals("Creation times should match", imageMetaData[0].getCreatedAt().toString(), retrievedItem.get("created_at").n() +".0");
        assertEquals("Last modification times should match", imageMetaData[0].getLastModified().toString(), retrievedItem.get("last_modified").n()+".0");
        assertEquals("Sizes should match", String.valueOf(imageMetaData[0].getObjectSize()), retrievedItem.get("object_size").n()+".0");
        assertEquals("Object types should match", imageMetaData[0].getObjectType(), retrievedItem.get("object_type").s());


// перевірка sqs
        SqsClient sqsClient = SqsClient.builder()
                .build();


        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(QueueUrl)
                .maxNumberOfMessages(5)
                .build();

        var messages = sqsClient.receiveMessage(receiveRequest).messages();

        for (var message : messages) {
            System.out.println("Received message: " + message.body());
        }

        sqsClient.close();

    }



    @After
    public void tearDown() {
        deleteFileWithId(uploadedFileId);
        System.out.println("The file with id " + uploadedFileId + " was deleted");
    }

    private String uploadFileToBucket(String pathToFile) {
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
        return jsonPath.get("id");

    }


    private String deleteFileWithId(String id) {
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

}
