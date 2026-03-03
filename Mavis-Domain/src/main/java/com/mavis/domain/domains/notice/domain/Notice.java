package com.mavis.domain.domains.notice.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    @Builder.Default
    private boolean isDeleted = false;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
