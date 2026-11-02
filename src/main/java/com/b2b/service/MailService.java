package com.b2b.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;

    // false par défaut : le mail est affiché dans la console au lieu d'être envoyé
    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public void sendResetPasswordEmail(String to, String token) {
        String text = """
        Hello,
        Use the following token to reset your password:
        %s
        If you did not request this, you can ignore this email.
        — B2B Platform
        """.formatted(token);

        if (!mailEnabled) {
            log.info("[MAIL DÉSACTIVÉ] Mail de réinitialisation pour {} :\n{}", to, text);
            return;
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Password reset token");
        msg.setText(text);
        mailSender.getObject().send(msg);
    }
}
