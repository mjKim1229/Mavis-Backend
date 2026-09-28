package com.mavis.domain.domains.claim.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ClaimReturn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("FK → claim.id (반품 클레임과 1:1)")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;

    @Comment("고객이 반품 발송한 택배사")
    @Column(nullable = false)
    private String carrier;

    @Comment("고객이 반품 발송한 송장번호")
    @Column(nullable = false)
    private String trackingNumber;

    public static ClaimReturn of(Claim claim, String carrier, String trackingNumber) {
        return new ClaimReturn(null, claim, carrier, trackingNumber);
    }
}
