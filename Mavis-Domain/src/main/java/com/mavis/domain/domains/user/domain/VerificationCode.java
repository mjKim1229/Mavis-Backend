package com.mavis.domain.domains.user.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

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

    @Builder.Default
    private boolean isDeleted = false;

    @Comment("인증번호 확인 완료 여부. 회원가입은 이 값이 true인 행이 있어야 진행 가능")
    @Builder.Default
    private boolean isVerified = false;

    public void delete() {
        this.isDeleted = true;
    }

    public void update(Integer code, LocalDateTime expiredAt) {
        this.code = code;
        this.expiredAt = expiredAt;
        this.isVerified = false;
    }

    /**
     * 인증번호 확인 완료. 회원가입 완료까지 쓸 수 있도록 만료 시각을 다시 잡는다.
     */
    public void verify(LocalDateTime expiredAt) {
        this.isVerified = true;
        this.expiredAt = expiredAt;
    }

    public boolean isUsableForSignUp(LocalDateTime now) {
        return isVerified && !isDeleted && expiredAt.isAfter(now);
    }
}
