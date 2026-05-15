package com.mavis.infrastructure.outer.email;

import com.mavis.infrastructure.outer.email.event.PasswordResetMailEvent;
import com.mavis.infrastructure.outer.email.event.VerifyMailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MailEventListener {
    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVerifyMail(VerifyMailEvent event) {
        mailService.sendVerifyEmail(event.to(), event.subject(), event.authCode());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetMail(PasswordResetMailEvent event) {
        mailService.sendPasswordResetEmail(event.to(), event.resetLink());
    }
}
