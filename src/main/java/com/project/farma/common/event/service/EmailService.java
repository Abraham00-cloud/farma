package com.project.farma.common.event.service;

import com.project.farma.common.event.dto.ManagerCreatedEvent;
import com.project.farma.common.event.dto.OrganisationRegisteredEvent;
import com.project.farma.common.event.dto.PasswordResetEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailService {

    private static final String FROM_NO_REPLY = "FARMA <noreply@farma.com.ng>";
    private static final String FROM_SUPPORT = "FARMA Support <support@farma.com.ng>";

    private final SpringTemplateEngine templateEngine;

    @Value("${app.frontend.url:https://www.farma.com.ng}")
    private String frontendUrl;

    // Pull the API key from Render environment variables
    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    @Async
    @EventListener
    public void handleOrganisationRegistration(OrganisationRegisteredEvent event) {
        try {
            Context context = new Context();
            context.setVariable("name", event.proprietorName());
            context.setVariable("orgName", event.orgName());
            context.setVariable("regNumber", event.registrationNumber());
            context.setVariable("orgType", event.organisationType().name());
            context.setVariable("email", event.email());
            context.setVariable("loginUrl", frontendUrl + "/auth/proprietor");

            String htmlContent = templateEngine.process("welcome-email", context);

            sendViaResendApi(event.email(), "Welcome to FARMA - Organisation Provisioned Successfully", htmlContent, FROM_NO_REPLY);
        } catch (Exception e) {
            System.err.println("Failed to dispatch welcome email: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String email, String resetUrl) {
        try {
            Context context = new Context();
            context.setVariable("resetUrl", resetUrl);

            String htmlContent = templateEngine.process("password-recovery", context);

            sendViaResendApi(email, "FARMA - Password Recovery Request", htmlContent, FROM_SUPPORT);
        } catch (Exception e) {
            System.err.println("Failed to dispatch password reset email: " + e.getMessage());
        }
    }

    @Async
    @EventListener
    public void handleManagerCreation(ManagerCreatedEvent event) {
        try {
            Context context = new Context();
            context.setVariable("managerName", event.managerName());
            context.setVariable("orgName", event.orgName());
            context.setVariable("email", event.email());
            context.setVariable("temporaryPassword", event.temporaryPassword());
            context.setVariable("managerLoginUrl", frontendUrl + "/auth/manager");

            String htmlContent = templateEngine.process("manager-welcome-email", context);

            sendViaResendApi(event.email(), "FARMA - Assigned Facility Manager Access & Credentials", htmlContent, FROM_NO_REPLY);
        } catch (Exception e) {
            System.err.println("Failed to dispatch manager welcome email: " + e.getMessage());
        }
    }

    // --- THE BLAZING FAST, FIREWALL-PROOF REST CALL ---
    private void sendViaResendApi(String to, String subject, String htmlContent, String from) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> requestBody = Map.of(
                "from", from,
                "to", List.of(to),
                "subject", subject,
                "html", htmlContent
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("Email successfully dispatched via HTTPS to " + to);
        } catch (Exception e) {
            System.err.println("Resend API rejected the request: " + e.getMessage());
        }
    }
}