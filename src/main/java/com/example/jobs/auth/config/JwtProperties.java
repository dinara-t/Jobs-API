package com.example.jobs.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long expiryMs;
    private String cookieName;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiryMs() {
        return expiryMs;
    }

    public void setExpiryMs(long expiryMs) {
        this.expiryMs = expiryMs;
    }

    public long getExpirySeconds() {
        if (expiryMs <= 0) return 0;
        return expiryMs / 1000;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }
}