package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.order.exception.InvalidOrderQuantityException;
import com.mavis.domain.domains.order.exception.OrderAmountExceededException;
import com.mavis.domain.domains.product.domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    private final Order order = Order.builder().build();
    private final Product product = Product.builder().name("테스트상품").price(30000).build();

    @Test
    void 총액은_단가와_수량의_곱이다() {
        OrderOption option = new OrderOption("black", 3);

        OrderItem orderItem = OrderItem.of(option, 30000, order, product);

        assertThat(orderItem.getUnitPrice()).isEqualTo(30000);
        assertThat(orderItem.getQuantity()).isEqualTo(3);
        assertThat(orderItem.getTotalPrice()).isEqualTo(90000);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void 수량이_1_미만이면_InvalidOrderQuantityException(int quantity) {
        OrderOption option = new OrderOption("black", quantity);

        assertThatThrownBy(() -> OrderItem.of(option, 30000, order, product))
                .isInstanceOf(InvalidOrderQuantityException.class);
    }

    @Test
    void 단가와_수량의_곱이_int_범위를_넘으면_음수로_뒤집히지_않고_OrderAmountExceededException() {
        // 30000 * 143166 = 4,294,980,000 > Integer.MAX_VALUE → int 곱셈이면 음수로 뒤집힘
        OrderOption option = new OrderOption("black", 143166);

        assertThatThrownBy(() -> OrderItem.of(option, 30000, order, product))
                .isInstanceOf(OrderAmountExceededException.class);
    }
}
