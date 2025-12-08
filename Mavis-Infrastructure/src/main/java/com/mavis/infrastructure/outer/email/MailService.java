package com.mavis.infrastructure.outer.email;

import com.mavis.common.properties.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    public void send(String to, String subject, String text) {
        MimeMessagePreparator messagePreparator =
                mimeMessage -> {
                    final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setFrom(mailProperties.username());
                    helper.setTo(to);
                    helper.setSubject(subject);
                    helper.setText(text, true);
                };
        javaMailSender.send(messagePreparator);
    }
}
