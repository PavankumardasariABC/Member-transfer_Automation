package com.membertransfer.e2e.http;

import io.restassured.RestAssured;
import io.restassured.config.ConnectionConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;

import java.util.concurrent.TimeUnit;

public final class RelaxedApi {

    private RelaxedApi() {
    }

    public static RequestSpecification with() {
        RestAssuredConfig config = RestAssuredConfig.config()
                .connectionConfig(ConnectionConfig.connectionConfig()
                        .closeIdleConnectionsAfterEachResponseAfter(10, TimeUnit.MINUTES))
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.socket.timeout", 600000)
                        .setParam("http.connection.timeout", 600000));
        return RestAssured.with().relaxedHTTPSValidation().config(config);
    }
}
