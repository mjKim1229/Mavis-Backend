package com.mavis.infrastructure.outer.email;

import com.mavis.common.properties.MailProperties;
import lombok.RequiredArgsConstructor;
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
    private final MailProperties mailProperties;
    private final TemplateEngine templateEngine;

    /**
     * 회원가입 인증번호 메일 발송
     */
    public void sendVerifyEmail(String to, String subject, String authCode) {
        Context context = new Context();
        context.setVariable("authCode", authCode);
        String html = templateEngine.process("mail/verify", context);

        send(to, subject, html);
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
            helper.setFrom(mailProperties.username());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
        };
        javaMailSender.send(messagePreparator);
    }
}
