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

import java.util.LinkedHashMap;
import java.util.Map;

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

    protected RequestBuilderWrapper csrfSpec() {
        CsrfSession csrf = fetchCsrfSession();
        return spec()
                .withCookies(csrf.cookies())
                .withHeader(csrf.headerName(), csrf.token());
    }

    protected RequestBuilderWrapper authenticatedSpec(Temp temp) {
        AuthSession authSession = loginAndGetSession(temp);

        return spec()
                .withCookies(authSession.cookies())
                .withHeader(authSession.headerName(), authSession.csrfToken());
    }

    private AuthSession loginAndGetSession(Temp temp) {
        CsrfSession csrf = fetchCsrfSession();

        Response response = RestAssured.given()
                .baseUri("http://localhost")
                .port(port)
                .contentType("application/json")
                .cookies(csrf.cookies())
                .header(csrf.headerName(), csrf.token())
                .body(new LoginBody(temp.getEmail(), testFixture.getPasswordFor(temp)))
                .when()
                .post("/auth/login");

        response.then().statusCode(204);

        Cookie jwtCookie = response.getDetailedCookie("jwt");
        if (jwtCookie == null || jwtCookie.getValue() == null || jwtCookie.getValue().isBlank()) {
            throw new IllegalStateException("JWT cookie was not returned by /auth/login");
        }

        Map<String, String> cookies = new LinkedHashMap<>(csrf.cookies());
        cookies.put("jwt", jwtCookie.getValue());

        return new AuthSession(
                jwtCookie.getValue(),
                csrf.token(),
                csrf.headerName(),
                cookies
        );
    }

    private CsrfSession fetchCsrfSession() {
        Response response = RestAssured.given()
                .baseUri("http://localhost")
                .port(port)
                .when()
                .get("/csrf/csrf-token");

        response.then().statusCode(200);

        String token = response.jsonPath().getString("token");
        String headerName = response.jsonPath().getString("headerName");

        Cookie csrfCookie = response.getDetailedCookie("XSRF-TOKEN");
        if (csrfCookie == null || csrfCookie.getValue() == null || csrfCookie.getValue().isBlank()) {
            throw new IllegalStateException("XSRF-TOKEN cookie was not returned by /csrf/csrf-token");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("CSRF token value was not returned by /csrf/csrf-token");
        }

        if (headerName == null || headerName.isBlank()) {
            throw new IllegalStateException("CSRF header name was not returned by /csrf/csrf-token");
        }

        Map<String, String> cookies = new LinkedHashMap<>();
        cookies.put("XSRF-TOKEN", csrfCookie.getValue());

        return new CsrfSession(token, headerName, cookies);
    }

    private record LoginBody(String username, String password) {
    }

    private record CsrfSession(
            String token,
            String headerName,
            Map<String, String> cookies
    ) {
    }

    private record AuthSession(
            String jwtToken,
            String csrfToken,
            String headerName,
            Map<String, String> cookies
    ) {
    }
}