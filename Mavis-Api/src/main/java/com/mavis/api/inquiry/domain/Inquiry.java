package com.mavis.api.inquiry.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String question;
    private String answer;
    private Long answerAdminId;
    private Long questionUserId;
    private String productId;
    private boolean isPrivate;

    @Builder.Default
    private boolean isDeleted = false;
}
