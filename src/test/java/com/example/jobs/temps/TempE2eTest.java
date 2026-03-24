package com.example.jobs.temps;

import com.example.jobs.E2eTestSuite;
import com.example.jobs.TestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class TempE2eTest extends E2eTestSuite {

    @Autowired
    public TempE2eTest(TestFixture testFixture) {
        super(testFixture);
    }

    @Test
    void listTemps_asManager_returnsOnlyDirectAndIndirectReports() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .get("/temps")
                .then()
                .statusCode(200)
                .body("items.size()", greaterThanOrEqualTo(2))
                .body("items.findAll { it.email == 'worker.alpha1@example.com' }.size()", equalTo(1))
                .body("items.findAll { it.email == 'worker.alpha2@example.com' }.size()", equalTo(1))
                .body("items.findAll { it.email == 'worker.beta1@example.com' }.size()", equalTo(0))
                .body("items.findAll { it.email == 'manager.alpha@example.com' }.size()", equalTo(0));
    }

    @Test
    void getTemp_asManager_forVisibleReport_returnsTempWithJobs() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .get("/temps/{id}", getTestFixture().getWorkerA1().getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(getTestFixture().getWorkerA1().getId().intValue()))
                .body("email", equalTo("worker.alpha1@example.com"))
                .body("jobs.size()", equalTo(1))
                .body("jobs[0].name", equalTo("Worker A1 Existing Job"));
    }

    @Test
    void getTemp_asManager_forInvisibleTemp_returnsNotFound() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .get("/temps/{id}", getTestFixture().getWorkerB1().getId())
                .then()
                .statusCode(404)
                .body("message", equalTo("Temp not found"));
    }

    @Test
    void listAvailableTempsForJob_excludesTempsWithOverlap() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .get("/temps?jobId={jobId}", getTestFixture().getWorkerA1ExistingJob().getId())
                .then()
                .statusCode(200)
                .body("items.findAll { it.email == 'worker.alpha1@example.com' }.size()", equalTo(1));
    }

    @Test
    void createTemp_underVisibleManager_succeeds() {
        Map<String, Object> dto = Map.of(
                "firstName", "New",
                "lastName", "Report",
                "email", "new.report@example.com",
                "password", "password12345",
                "managerId", getTestFixture().getManagerA().getId()
        );

        authenticatedSpec(getTestFixture().getAdmin())
                .build()
                .when()
                .body(dto)
                .post("/temps")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("New"))
                .body("lastName", equalTo("Report"))
                .body("email", equalTo("new.report@example.com"))
                .body("managerId", equalTo(getTestFixture().getManagerA().getId().intValue()));
    }

    @Test
    void getProfile_returnsCurrentTemp() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .get("/profile")
                .then()
                .statusCode(200)
                .body("email", equalTo("manager.alpha@example.com"));
    }

    @Test
    void patchProfile_updatesCurrentTemp() {
        Map<String, Object> dto = Map.of(
                "firstName", "ManagerUpdated",
                "lastName", "AlphaUpdated",
                "email", "manager.alpha@example.com"
        );

        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .body(dto)
                .patch("/profile")
                .then()
                .statusCode(200)
                .body("firstName", equalTo("ManagerUpdated"))
                .body("lastName", equalTo("AlphaUpdated"))
                .body("email", equalTo("manager.alpha@example.com"));
    }
}