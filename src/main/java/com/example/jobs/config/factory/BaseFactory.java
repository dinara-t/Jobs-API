package com.example.jobs.config.factory;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.javafaker.Faker;

public abstract class BaseFactory {

    private final Faker faker = new Faker();
    private static final AtomicInteger counter = new AtomicInteger();

    protected Faker faker() {
        return faker;
    }

    protected int incrementAndGet() {
        return counter.incrementAndGet();
    }

    public abstract boolean repoEmpty();
}