package com.mavis.domain.domains.review.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int score;
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean isPrivate;

    @OneToMany(mappedBy = "review")
    private List<ReviewImage> images;

    @Builder.Default
    private boolean isDeleted = false;

    public void delete() {
        this.isDeleted = true;
    }

    public void update(int score, String content, boolean isPrivate) {
        this.score = score;
        this.content = content;
        this.isPrivate = isPrivate;
    }

    public String getContentForPublic() {
        if (isPrivate) {
            return null;
        }
        return content;
    }
}
