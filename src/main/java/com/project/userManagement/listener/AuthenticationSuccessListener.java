package com.project.userManagement.listener;

import com.project.userManagement.domain.UserPrincipal;
import com.project.userManagement.service.LoginAttempt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener {
    @Autowired
    private LoginAttempt loginAttempt;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            UserPrincipal user = (UserPrincipal) event.getAuthentication().getPrincipal();
            loginAttempt.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }
}
