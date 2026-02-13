package com.elevideo.backend.service.impl;

import com.elevideo.backend.dto.email.EmailRequest;
import com.elevideo.backend.dto.email.EmailResponse;
import com.elevideo.backend.model.User;
import com.elevideo.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final TemplateEngine templateEngine;
    private final RestClient resendRestClient;

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Value("${resend.from-name}")
    private String fromName;

    @Override
    public void sendEmailTemplate(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("📧 Preparando correo a '{}' usando plantilla '{}'", to, templateName);
        String htmlContent = renderTemplate(templateName, variables);
        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String html) {
        try {
            EmailRequest request = new EmailRequest(
                    String.format("%s <%s>", fromName, fromEmail),
                    to,
                    subject,
                    html
            );

            EmailResponse response = resendRestClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(EmailResponse.class);

            log.info("✅ Correo enviado a '{}'. ID: {}", to, response != null ? response.id() : "N/A");

        } catch (RestClientException e) {
            log.error("❌ Error enviando correo a '{}': {}", to, e.getMessage());
            throw e;
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

}
