package com.example.jobs.auth;

import com.example.jobs.E2eTestSuite;
import com.example.jobs.TestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class AuthE2eTest extends E2eTestSuite {

    @Autowired
    public AuthE2eTest(TestFixture testFixture) {
        super(testFixture);
    }

    @Test
    void login_withValidCredentials_returnsNoContentAndJwtCookie() {
        spec()
                .build()
                .body("""
                        {
                          "username": "manager.alpha@example.com",
                          "password": "password12345"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(204)
                .header("Set-Cookie", notNullValue())
                .header("Set-Cookie", containsString("jwt="));
    }

    @Test
    void login_withInvalidCredentials_returnsUnauthorized() {
        spec()
                .build()
                .body("""
                        {
                          "username": "manager.alpha@example.com",
                          "password": "wrong-password"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)
                .body("message", equalTo("Invalid credentials"))
                .body("path", equalTo("/auth/login"));
    }

    @Test
    void getProtectedRoute_withoutJwt_returnsUnauthorized() {
        spec()
                .build()
                .when()
                .get("/jobs")
                .then()
                .statusCode(401)
                .body("path", equalTo("/jobs"));
    }

    @Test
    void logout_returnsNoContent() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .post("/auth/logout")
                .then()
                .statusCode(204)
                .header("Set-Cookie", containsString("Max-Age=0"));
    }
}