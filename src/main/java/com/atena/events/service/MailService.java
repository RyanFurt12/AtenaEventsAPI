package com.atena.events.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de emails transacionais (reset de senha, confirmação de troca de email).
 *
 * Usa o {@link JavaMailSender} do Spring — a configuração de SMTP vem das
 * propriedades {@code spring.mail.*}. Em dev/docker aponta para o MailHog;
 * em produção basta sobrescrever as variáveis MAIL_* para um SMTP real,
 * sem qualquer mudança neste código.
 */
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;

    public MailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    public void sendPasswordReset(String toEmail, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("AtenaEvents — Redefinição de senha");
        message.setText(
            "Recebemos um pedido para redefinir a sua senha.\n\n" +
            "Para criar uma nova senha, acesse o link abaixo:\n" +
            link + "\n\n" +
            "Se você não solicitou isso, ignore este email — sua senha permanece inalterada.\n" +
            "Este link expira em breve."
        );
        mailSender.send(message);
    }

    public void sendEmailChangeConfirmation(String toEmail, String token) {
        String link = frontendUrl + "/confirm-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("AtenaEvents — Confirme seu novo email");
        message.setText(
            "Você pediu para alterar o email da sua conta para este endereço.\n\n" +
            "Para confirmar a troca, acesse o link abaixo:\n" +
            link + "\n\n" +
            "Se você não solicitou isso, ignore este email — nada será alterado.\n" +
            "Este link expira em breve."
        );
        mailSender.send(message);
    }
}
