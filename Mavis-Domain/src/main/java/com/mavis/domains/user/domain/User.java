package com.mavis.domains.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String snsId;

    private String name;

    private String nickname;

    private String email;

    private String gender;

    private String birthDay;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private SnsType snsType;
}
