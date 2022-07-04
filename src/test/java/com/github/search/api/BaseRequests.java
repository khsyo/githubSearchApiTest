package com.github.search.api;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;

import static com.github.search.api.SpecBuilder.*;
import static io.restassured.RestAssured.given;

public class BaseRequests {
    public static Response get(HashMap queryParams){
        return given().spec(getRequestSpec()).
                urlEncodingEnabled(false).
                queryParams(queryParams).
        when().
                get().
        then().spec(getResponseSpec()).
                assertThat().
                statusCode(200).
                extract().
                response();
    }

    public static Response getAndExpectError(HashMap queryParams, Integer errorCode){
        return given().spec(getRequestSpec()).
                urlEncodingEnabled(false).
                queryParams(queryParams).
                when().
                get().
                then().spec(getResponseSpec()).
                assertThat().
                statusCode(errorCode).
                extract().
                response();
    }



    public static void getAndValidateSchema(HashMap queryParams, String resourceFile) throws URISyntaxException {
        URL res = BaseRequests.class.getClassLoader().getResource(resourceFile);
        File file = Paths.get(res.toURI()).toFile();

        given().spec(getRequestSpec()).
                urlEncodingEnabled(false).
                queryParams(queryParams).
        when().
                get().
        then().spec(getResponseSpec()).
                assertThat().
                body(JsonSchemaValidator.matchesJsonSchema(file));
    }

}
