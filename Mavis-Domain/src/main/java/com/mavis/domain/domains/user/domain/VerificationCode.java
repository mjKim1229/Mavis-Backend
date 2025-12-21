package com.mavis.domain.domains.user.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer code;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;

    private LocalDateTime expiredAt;

    private String email;

    private boolean isDeleted = false;

    public void delete() {
        this.isDeleted = true;
    }

    public void update(Integer code, LocalDateTime expiredAt) {
        this.code = code;
        this.expiredAt = expiredAt;
    }
}
