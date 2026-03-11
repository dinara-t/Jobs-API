package com.example.jobs.temps.entities;

import java.util.ArrayList;
import java.util.List;

import com.example.jobs.jobs.entities.Job;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "temps")
public class Temp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Temp manager;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private List<Temp> reports = new ArrayList<>();

    @OneToMany(mappedBy = "temp", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Job> jobs = new ArrayList<>();

    public Temp() {
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Temp getManager() {
        return manager;
    }

    public List<Temp> getReports() {
        return reports;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setManager(Temp manager) {
        this.manager = manager;
    }

    public void setReports(List<Temp> reports) {
        this.reports = reports;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}