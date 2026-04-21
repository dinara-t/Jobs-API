package com.example.jobs;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

public class RequestBuilderWrapper {

    private final RequestSpecBuilder requestSpecBuilder;
    private final Map<String, String> cookies = new LinkedHashMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();

    public RequestBuilderWrapper(int port, String baseUri) {
        this.requestSpecBuilder = new RequestSpecBuilder()
                .setPort(port)
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON);
    }

    public RequestBuilderWrapper withCookie(String name, String value) {
        if (name != null && !name.isBlank() && value != null && !value.isBlank()) {
            cookies.put(name, value);
        }
        return this;
    }

    public RequestBuilderWrapper withCookies(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return this;
        }

        values.forEach(this::withCookie);
        return this;
    }

    public RequestBuilderWrapper withHeader(String name, String value) {
        if (name != null && !name.isBlank() && value != null && !value.isBlank()) {
            headers.put(name, value);
        }
        return this;
    }

    public RequestSpecification build() {
        RequestSpecification spec = RestAssured.given().spec(requestSpecBuilder.build());

        if (!cookies.isEmpty()) {
            spec.cookies(cookies);
        }

        if (!headers.isEmpty()) {
            spec.headers(headers);
        }

        return spec;
    }
}