# Jobs Workforce Management API

A secure Spring Boot REST API powering a workforce scheduling and job management platform.

This application provides workforce hierarchy management, job scheduling, assignment validation, authentication, and business rule enforcement for temporary workforce operations.

---

## Project Overview

The API was designed to support workforce management scenarios where:

- Managers supervise multiple workers
- Jobs require worker assignments
- Scheduling conflicts must be prevented
- Access control must be enforced
- Workforce visibility follows organisational hierarchy

The project evolved from a CRUD application into a production-ready backend with authentication, security, automated testing, and cloud deployment.

---

## Core Features

### Authentication & Security

- Spring Security
- JWT Authentication
- Secure HttpOnly Cookies
- CSRF Protection
- BCrypt Password Hashing
- Protected Endpoints
- CORS Configuration

### Workforce Management

- Create workers
- Update workers
- Delete workers
- Workforce hierarchy management
- Recursive manager visibility

### Job Management

- Create jobs
- Update jobs
- Delete jobs
- Assign workers
- Unassign workers
- Availability validation
- Overlapping assignment prevention

### API Features

- RESTful API design
- Pagination
- Sorting
- Filtering
- DTO mapping
- Validation
- Global exception handling

### Documentation

- OpenAPI / Swagger

---

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate

### Database

- MySQL
- Amazon Aurora
- Amazon RDS

### Testing

- JUnit 5
- Rest Assured
- H2 Database

### Build & CI

- Maven
- GitHub Actions

---

## Architecture

```text
Controller Layer
        │
        ▼
Service Layer
        │
        ▼
Repository Layer
        │
        ▼
MySQL / Aurora
```

---

## Cloud Deployment

Production infrastructure includes:

- AWS Elastic Beanstalk
- Amazon Aurora
- Amazon RDS
- AWS Route 53
- AWS Certificate Manager
- HTTPS Custom Domain

This deployment provided hands-on experience with real-world cloud infrastructure rather than local-only development.

---

## Testing Strategy

The project includes:

### Unit Testing

- Service layer testing
- Validation testing
- Business rule testing

### Integration Testing

- Repository testing
- Database interaction testing

### End-to-End Testing

- Rest Assured
- Full API request lifecycle testing

### Continuous Integration

GitHub Actions automatically:

- Builds the application
- Executes tests
- Validates pull requests

---

## Security Highlights

Implemented modern security practices including:

- JWT authentication
- Secure cookie storage
- CSRF protection
- Password hashing
- Role-based access control
- Endpoint protection

---

## What I Learned

This project became my primary backend learning platform.

Major learning outcomes:

- Designing REST APIs
- Layered application architecture
- Spring Security implementation
- Authentication and authorisation
- Database design
- ORM with JPA and Hibernate
- Writing integration tests
- Automated CI pipelines
- AWS deployment workflows
- Production debugging
- Security best practices
- Managing business rules within service layers

Most importantly, I learned how to evolve a project from a simple CRUD application into a production-ready backend system.

---

## Future Improvements

- Event-driven architecture
- Audit logging
- Worker notifications
- Advanced reporting
- Role management
- Multi-tenant support