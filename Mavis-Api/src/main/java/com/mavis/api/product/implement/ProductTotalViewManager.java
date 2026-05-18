package com.mavis.api.product.implement;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductTotalView;
import com.mavis.domain.domains.product.repository.ProductTotalViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
@RequiredArgsConstructor
public class ProductTotalViewManager {
    private final ProductTotalViewRepository productTotalViewRepository;

    @Transactional
    public void increaseTotalView(Product product) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        long updated = productTotalViewRepository.increaseViewCount(product, weekStart, weekEnd);

        if (updated == 0) {
            productTotalViewRepository.save(ProductTotalView.builder()
                    .weekStart(weekStart)
                    .weekEnd(weekEnd)
                    .product(product)
                    .build());
        }
    }
}
