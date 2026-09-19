package com.mavis.api.order.implement;

import com.mavis.api.order.dto.OrderItemRequest;
import com.mavis.api.order.dto.OrderOptionRequest;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.exception.OrderAmountExceededException;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.implement.ProductReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderItemAppenderTest {

    @InjectMocks
    private OrderItemAppender orderItemAppender;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductReader productReader;

    @Test
    void 여러_상품의_수량과_단가를_곱한_합산_금액을_반환한다() {
        // given
        Product productA = Product.builder().id(1L).price(10000).build();
        Product productB = Product.builder().id(2L).price(5000).build();

        OrderItemRequest itemA = new OrderItemRequest(1L, new OrderOptionRequest("black", 2));
        OrderItemRequest itemB = new OrderItemRequest(2L, new OrderOptionRequest("white", 3));

        Order order = Order.builder().build();

        given(productReader.readById(1L)).willReturn(productA);
        given(productReader.readById(2L)).willReturn(productB);

        // when
        int totalPrice = orderItemAppender.saveOrderItems(List.of(itemA, itemB), order);

        // then
        // productA: 10000 * 2 = 20000
        // productB: 5000 * 3 = 15000
        // 합산: 35000
        assertThat(totalPrice).isEqualTo(35000);
    }

    @Test
    void 품목별_금액은_정상이어도_합계가_int_범위를_넘으면_OrderAmountExceededException() {
        // given: 각 품목 1,073,741,824원(정상 범위) — 합계 2,147,483,648원은 Integer.MAX_VALUE 초과
        int halfOverMax = Integer.MAX_VALUE / 2 + 1;
        Product productA = Product.builder().id(1L).price(halfOverMax).build();
        Product productB = Product.builder().id(2L).price(halfOverMax).build();

        OrderItemRequest itemA = new OrderItemRequest(1L, new OrderOptionRequest("black", 1));
        OrderItemRequest itemB = new OrderItemRequest(2L, new OrderOptionRequest("white", 1));

        Order order = Order.builder().build();

        given(productReader.readById(1L)).willReturn(productA);
        given(productReader.readById(2L)).willReturn(productB);

        // when & then
        assertThatThrownBy(() -> orderItemAppender.saveOrderItems(List.of(itemA, itemB), order))
                .isInstanceOf(OrderAmountExceededException.class);
        verify(orderItemRepository, never()).saveAll(any());
    }
}
