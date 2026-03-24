package com.example.jobs;

import com.example.jobs.temps.entities.Temp;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class E2eTestSuite {

    @LocalServerPort
    private int port;

    private final TestFixture testFixture;

    protected E2eTestSuite(TestFixture testFixture) {
        this.testFixture = testFixture;
    }

    protected TestFixture getTestFixture() {
        return testFixture;
    }

    protected RequestBuilderWrapper spec() {
        return new RequestBuilderWrapper(port, "http://localhost");
    }

    protected RequestBuilderWrapper authenticatedSpec(Temp temp) {
        String jwtCookie = loginAndGetJwt(temp);
        return spec().withCookie("jwt", jwtCookie);
    }

    private String loginAndGetJwt(Temp temp) {
        Response response = RestAssured.given()
                .baseUri("http://localhost")
                .port(port)
                .contentType("application/json")
                .body(new LoginBody(temp.getEmail(), testFixture.getPasswordFor(temp)))
                .when()
                .post("/auth/login");

        response.then().statusCode(204);

        Cookie jwtCookie = response.getDetailedCookie("jwt");
        if (jwtCookie == null || jwtCookie.getValue() == null || jwtCookie.getValue().isBlank()) {
            throw new IllegalStateException("JWT cookie was not returned by /auth/login");
        }

        return jwtCookie.getValue();
    }

    private record LoginBody(String username, String password) {
    }
}