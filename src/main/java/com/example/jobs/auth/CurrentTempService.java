package com.example.jobs.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.jobs.common.exception.UnauthorizedException;
import com.example.jobs.temps.TempRepository;
import com.example.jobs.temps.entities.Temp;

@Service
public class CurrentTempService {

    private final TempRepository tempRepository;

    public CurrentTempService(TempRepository tempRepository) {
        this.tempRepository = tempRepository;
    }

    public Temp getCurrentTempEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetailsImpl u) {
            Long tempId = u.getTemp().getId();
            if (tempId == null) {
                throw new UnauthorizedException("Unauthorized");
            }

            return tempRepository.findById(tempId)
                    .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
        }

        String email = principal.toString();
        return tempRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
    }
}