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
public class ClaimImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("FK → claim.id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Comment("고객이 첨부한 사진 S3 URL")
    @Column(nullable = false)
    private String imageUrl;

    static ClaimImage of(Claim claim, String imageUrl) {
        return new ClaimImage(null, claim, imageUrl);
    }
}
