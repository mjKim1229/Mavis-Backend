package com.mavis.api.product.implement;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductTotalView;
import com.mavis.domain.domains.product.repository.ProductTotalViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
@RequiredArgsConstructor
public class ProductTotalViewManager {
    private final ProductTotalViewRepository productTotalViewRepository;

    public void increaseTotalView(Product product) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        productTotalViewRepository.findByProductAndWeekStartAndWeekEndAndIsDeletedFalse(product, weekStart, weekEnd)
                .ifPresentOrElse(
                        ProductTotalView::increaseView,
                        () -> saveNewProductTotalView(product, weekStart, weekEnd)
                );

    }

    private void saveNewProductTotalView(Product product, LocalDate weekStart, LocalDate weekEnd) {
        ProductTotalView productTotalView = ProductTotalView.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .product(product)
                .build();
        productTotalViewRepository.save(productTotalView);
    }
}
