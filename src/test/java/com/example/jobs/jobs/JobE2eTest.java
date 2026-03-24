package com.example.jobs.jobs;

import com.example.jobs.E2eTestSuite;
import com.example.jobs.TestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class JobE2eTest extends E2eTestSuite {

    @Autowired
    public JobE2eTest(TestFixture testFixture) {
        super(testFixture);
    }

    @Test
    void listJobs_asManager_returnsOnlyVisibleJobs() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .get("/jobs")
                .then()
                .statusCode(200)
                .body("items.size()", greaterThanOrEqualTo(2))
                .body("items.findAll { it.name == 'Invisible Assigned Job' }.size()", equalTo(0))
                .body("items.findAll { it.name == 'Unassigned Visible Job' }.size()", equalTo(1))
                .body("items.findAll { it.name == 'Visible Assigned Job' }.size()", equalTo(1));
    }

    @Test
    void getJob_asManager_forInvisibleJob_returnsNotFound() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .get("/jobs/{id}", getTestFixture().getInvisibleAssignedJob().getId())
                .then()
                .statusCode(404)
                .body("message", equalTo("Job not found"));
    }

    @Test
    void createJob_withVisibleAssignedTemp_succeeds() {
        Map<String, Object> dto = Map.of(
                "name", "Fresh Visible Assignment",
                "startDate", LocalDate.of(2026, 6, 10).toString(),
                "endDate", LocalDate.of(2026, 6, 12).toString(),
                "tempId", getTestFixture().getWorkerA2().getId()
        );

        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .body(dto)
                .post("/jobs")
                .then()
                .statusCode(200)
                .body("name", equalTo("Fresh Visible Assignment"))
                .body("temp.id", equalTo(getTestFixture().getWorkerA2().getId().intValue()));
    }

    @Test
    void createJob_withOverlappingAssignedTemp_returnsBadRequest() {
        Map<String, Object> dto = Map.of(
                "name", "Overlapping Job",
                "startDate", LocalDate.of(2026, 5, 2).toString(),
                "endDate", LocalDate.of(2026, 5, 4).toString(),
                "tempId", getTestFixture().getWorkerA1().getId()
        );

        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .body(dto)
                .post("/jobs")
                .then()
                .statusCode(400)
                .body("message", equalTo("Temp already has an overlapping job"));
    }

    @Test
    void patchJob_assignVisibleTemp_succeeds() {
        Map<String, Object> dto = Map.of(
                "tempId", getTestFixture().getWorkerA1().getId()
        );

        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .body(dto)
                .patch("/jobs/{id}", getTestFixture().getUnassignedVisibleJob().getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(getTestFixture().getUnassignedVisibleJob().getId().intValue()))
                .body("temp.id", equalTo(getTestFixture().getWorkerA1().getId().intValue()));
    }

    @Test
    void patchJob_unassignWithTempIdZero_succeeds() {
        Map<String, Object> dto = Map.of(
                "tempId", 0
        );

        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .body(dto)
                .patch("/jobs/{id}", getTestFixture().getVisibleAssignedJob().getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(getTestFixture().getVisibleAssignedJob().getId().intValue()))
                .body("temp", equalTo(null));
    }

    @Test
    void listAssignedJobs_filterWorks() {
        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .get("/jobs?assigned=true")
                .then()
                .statusCode(200)
                .body("items.findAll { it.temp == null }.size()", equalTo(0));
    }

    @Test
    void createJob_withInvalidDateRange_returnsBadRequest() {
        Map<String, Object> dto = Map.of(
                "name", "Broken Date Range",
                "startDate", LocalDate.of(2026, 7, 10).toString(),
                "endDate", LocalDate.of(2026, 7, 9).toString()
        );

        authenticatedSpec(getTestFixture().getManagerA())
                .build()
                .when()
                .body(dto)
                .post("/jobs")
                .then()
                .statusCode(400)
                .body("message", equalTo("End date must be on or after start date"));
    }
}