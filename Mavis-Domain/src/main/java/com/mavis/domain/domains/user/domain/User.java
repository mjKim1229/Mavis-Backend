package com.mavis.domain.domains.user.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String snsId;

    private String name;

    private String nickname;

    private String email;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthDay;

    private String phoneNumber;

    private String age;

    private String profileImage;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private SnsType snsType;

    private String username;

    private String password;

    private String naverRefreshToken;

    @Embedded
    private DeliveryAddress defaultDeliveryAddress;

    @Embedded
    private MarketingAgreement marketingAgreement;

    @Builder.Default
    private boolean isDeleted = false;

    public void withDraw() {
        this.isDeleted = true;
        this.snsId = null;
        this.name = null;
        this.nickname = null;
        this.email = null;
        this.gender = null;
        this.birthDay = null;
        this.phoneNumber = null;
        this.defaultDeliveryAddress = null;
    }

    public void updateAddress(DeliveryAddress defaultDeliveryAddress) {
        this.defaultDeliveryAddress = defaultDeliveryAddress;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String nickname, String name, Gender gender, LocalDate birthDay, String phoneNumber) {
        this.nickname = nickname;
        this.name = name;
        this.gender = gender;
        this.birthDay = birthDay;
        this.phoneNumber = phoneNumber;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void updateEmailAgreement(boolean isEmailAgreed) {
        this.marketingAgreement.updateEmailAgreement(isEmailAgreed);
    }

    public void updateSmsAgreement(boolean isSmsAgreed) {
        this.marketingAgreement.updateSmsAgreement(isSmsAgreed);
    }
}
