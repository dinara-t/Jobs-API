package com.example.jobs.auth;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Unauthorized");
        }

        Object principal = auth.getPrincipal();

        if (principal == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        if (principal instanceof UserDetailsImpl userDetails) {
            Long tempId = userDetails.getTemp().getId();

            if (tempId == null) {
                throw new UnauthorizedException("Unauthorized");
            }

            return tempRepository.findById(tempId)
                    .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
        }

        if (principal instanceof String principalName) {
            if (principalName.isBlank() || "anonymousUser".equalsIgnoreCase(principalName)) {
                throw new UnauthorizedException("Unauthorized");
            }

            return tempRepository.findByEmail(principalName)
                    .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
        }

        throw new UnauthorizedException("Unauthorized");
    }
}