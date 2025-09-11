package com.mavis.api.review.domain;

import com.mavis.api.common.jpa.BaseEntity;
import com.mavis.api.order.domain.Order;
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
    @JoinColumn(name = "order_id")
    private Order order;
    private Long userId;

    @OneToMany(mappedBy = "review")
    private List<ReviewImage> images;
}
