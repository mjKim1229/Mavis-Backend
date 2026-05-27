package com.mavis.domain.domains.banner.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BannerImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private int sortOrder;

    @Builder.Default
    private boolean isDeleted = false;

    public void update(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
