package com.mavis.domain.domains.inquiry.domain;

import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
public class InquiryAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String answer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Builder.Default
    private boolean isDeleted = false;

    public void updateAnswer(String answer) {
        this.answer = answer;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
