package com.mavis.domain.domains.product.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductNotice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String precaution;

    private String shippingInfo;

    private String returnRequest;

    private String returnProcess;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public void update(String precaution, String shippingInfo, String returnRequest, String returnProcess) {
        this.precaution = precaution;
        this.shippingInfo = shippingInfo;
        this.returnRequest = returnRequest;
        this.returnProcess = returnProcess;
    }
}
