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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean isPrivate;

    @OneToMany(mappedBy = "review")
    private List<ReviewImage> images;

    @Builder.Default
    private final boolean isDeleted = false;

    public String getContentForPublic() {
        if (isPrivate) {
            return null;
        }
        return content;
    }
}
