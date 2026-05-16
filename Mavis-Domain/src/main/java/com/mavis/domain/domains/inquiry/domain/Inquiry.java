package com.mavis.domain.domains.inquiry.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@AllArgsConstructor
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private boolean isPrivate;

    @OneToOne(mappedBy = "inquiry")
    private InquiryAnswer inquiryAnswer;

    @Builder.Default
    private boolean isDeleted = false;

    public void delete() {
        this.isDeleted = true;
        if (this.inquiryAnswer != null) {
            this.inquiryAnswer.delete();
        }
    }

}
