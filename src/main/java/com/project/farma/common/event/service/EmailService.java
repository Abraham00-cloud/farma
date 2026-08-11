package com.project.farma.common.event.service;

import com.project.farma.common.event.dto.ManagerCreatedEvent;
import com.project.farma.common.event.dto.OrganisationRegisteredEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.frontend.url:https://www.farma.com.ng}") // Fallback to production domain if not specified
    private String frontendUrl;

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

            // Updated to use production domain
            context.setVariable("loginUrl", frontendUrl + "/auth/proprietor");

            String htmlContent = templateEngine.process("welcome-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("onboarding@resend.dev");
            helper.setTo(event.email());
            helper.setSubject("Welcome to FARMA - Organisation Provisioned Successfully");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to dispatch welcome email: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String recipientEmail, String resetUrl) {
        try {
            Context context = new Context();
            // Note: If resetUrl is passed directly from the caller, ensure it uses frontendUrl too!
            context.setVariable("resetUrl", resetUrl);

            String htmlContent = templateEngine.process("password-reset-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("support@resend.dev");
            helper.setTo(recipientEmail);
            helper.setSubject("FARMA - Password Recovery Request");
            helper.setText(htmlContent, true);

            mailSender.send(message);
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

            // Updated to use production domain link pointing directly to manager portal login
            context.setVariable("managerLoginUrl", frontendUrl + "/auth/manager");

            String htmlContent = templateEngine.process("manager-welcome-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("onboarding@resend.dev");
            helper.setTo(event.email());
            helper.setSubject("FARMA - Assigned Facility Manager Access & Credentials");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to dispatch manager welcome email: " + e.getMessage());
        }
    }
}