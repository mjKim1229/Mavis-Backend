package com.mavis.domain.domains.user.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationCode extends BaseEntity {

    @Id
    private Long id;

    private Integer code;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private VerificationType verificationType;

    private String email;

    private boolean isDeleted = false;

    void delete() {
        this.isDeleted = true;
    }
}
