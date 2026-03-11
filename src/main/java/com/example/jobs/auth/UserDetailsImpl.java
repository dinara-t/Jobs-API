package com.example.jobs.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.jobs.temps.entities.Temp;

public class UserDetailsImpl implements UserDetails {

    private final Temp temp;

    public UserDetailsImpl(Temp temp) {
        this.temp = temp;
    }

    public Temp getTemp() {
        return temp;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return temp.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return temp.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}