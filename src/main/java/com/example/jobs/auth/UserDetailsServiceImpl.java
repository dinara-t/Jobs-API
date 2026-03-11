package com.example.jobs.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.jobs.temps.TempRepository;
import com.example.jobs.temps.entities.Temp;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final TempRepository tempRepository;

    public UserDetailsServiceImpl(TempRepository tempRepository) {
        this.tempRepository = tempRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Temp temp = tempRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserDetailsImpl(temp);
    }
}