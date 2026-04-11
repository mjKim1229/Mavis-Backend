package com.mavis.api.order.implement;

import com.mavis.api.order.dto.OrderItemRequest;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.order.domain.Order;
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
import static org.mockito.BDDMockito.given;

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

        OrderItemRequest itemA = new OrderItemRequest(1L, new OrderOption("black", 2));
        OrderItemRequest itemB = new OrderItemRequest(2L, new OrderOption("white", 3));

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
}
