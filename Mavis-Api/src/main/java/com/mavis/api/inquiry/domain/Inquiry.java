package com.mavis.api.inquiry.domain;

import com.mavis.api.common.jpa.BaseEntity;
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
public class Inquiry extends BaseEntity {

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
