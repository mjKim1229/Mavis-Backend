package com.mavis.api.inquiry.domain;

import com.mavis.api.common.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String question;
    private String answer;
    private Long answerAdminId;
    private Long questionUserId;
    private Long productId;
    private boolean isPrivate;
    private LocalDateTime answeredDatetime;
    @Builder.Default
    private boolean isDeleted = false;
}
