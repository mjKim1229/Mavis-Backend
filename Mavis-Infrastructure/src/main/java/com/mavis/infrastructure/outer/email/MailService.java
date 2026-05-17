package com.mavis.infrastructure.outer.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * 회원가입 인증번호 메일 발송
     */
    public void sendVerifyEmail(String to, String subject, String authCode) {
        Context context = new Context();
        context.setVariable("authCode", authCode);
        String html = templateEngine.process("mail/verify", context);

        send(to, subject, html);
    }

    public void sendFindUsernameEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        String html = templateEngine.process("mail/find-username", context);

        send(to, "[가람몰] 아이디 찾기 안내", html);
    }

    /**
     * 비밀번호 재설정 링크 메일 발송
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        String html = templateEngine.process("mail/reset-password", context);

        send(to, "[가람몰] 비밀번호 재설정 링크 안내", html);
    }

    private void send(String to, String subject, String content) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
        };
        javaMailSender.send(messagePreparator);
    }
}
