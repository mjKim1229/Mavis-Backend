package com.mavis.domain.domains.user.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

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

    private String gender;

    private LocalDate birthDay;

    private String phoneNumber;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private SnsType snsType;

    private String username;

    private String password;

    private String naverRefreshToken;

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
    }
}
