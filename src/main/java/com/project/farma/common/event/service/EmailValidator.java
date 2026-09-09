package com.project.farma.common.event.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

public class EmailValidator {
    private static final List<String> BLOCKED_DOMAINS = List.of(
            "mailinator.com", "temp-mail.org", "guerrillamail.com", "10minutemail.com", "yopmail.com"
    );

    public static void validateOriginalEmail(String email) {
        if (email == null || !email.contains("@")) return;

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Disposable email addresses are not permitted.");
        }
    }
}