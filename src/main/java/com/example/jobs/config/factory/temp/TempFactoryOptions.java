package com.example.jobs.config.factory.temp;

import com.example.jobs.temps.entities.Temp;

public class TempFactoryOptions {

    public String firstName;
    public String lastName;
    public String email;
    public String rawPassword;
    public Temp manager;

    public TempFactoryOptions() {
    }

    public TempFactoryOptions firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public TempFactoryOptions lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public TempFactoryOptions email(String email) {
        this.email = email;
        return this;
    }

    public TempFactoryOptions rawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
        return this;
    }

    public TempFactoryOptions manager(Temp manager) {
        this.manager = manager;
        return this;
    }
}