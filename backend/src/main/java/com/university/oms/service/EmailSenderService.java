package com.university.oms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
public class EmailSenderService {
    private final JavaMailSender javaMailSender;
    private final boolean enabled;
    private final String host;
    private final String username;
    private final String password;
    private final String fromName;

    public EmailSenderService(JavaMailSender javaMailSender,
                              @Value("${oms.mail.external-enabled:false}") boolean enabled,
                              @Value("${spring.mail.host:}") String host,
                              @Value("${spring.mail.username:}") String username,
                              @Value("${spring.mail.password:}") String password,
                              @Value("${oms.mail.from-name:Office Management System}") String fromName) {
        this.javaMailSender = javaMailSender;
        this.enabled = enabled;
        this.host = host;
        this.username = username;
        this.password = password;
        this.fromName = fromName;
    }

    public boolean isEnabled() {
        return enabled && hasText(host) && hasText(username) && hasText(password);
    }

    public String disabledReason() {
        if (!enabled) {
            return "external mail disabled";
        }
        return "external mail disabled or SMTP configuration missing";
    }

    public String getFromName() {
        return fromName;
    }

    public void sendMail(String toEmail, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, false);
            helper.setFrom(new InternetAddress(username, fromName));
            javaMailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
