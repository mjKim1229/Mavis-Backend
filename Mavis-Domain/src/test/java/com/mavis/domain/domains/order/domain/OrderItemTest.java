package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.order.exception.InvalidOrderColorException;
import com.mavis.domain.domains.order.exception.InvalidOrderQuantityException;
import com.mavis.domain.domains.order.exception.OrderAmountExceededException;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    private final Order order = Order.builder().build();

    private Product productWithColors(String... colors) {
        Product product = Product.builder()
                .name("테스트상품")
                .price(30000)
                .colors(new ArrayList<>())
                .build();
        List<ProductColor> productColors = product.getColors();
        for (String color : colors) {
            productColors.add(ProductColor.of(product, color));
        }
        return product;
    }

    @Test
    void 총액은_단가와_수량의_곱이다() {
        Product product = productWithColors("블랙", "베이지");
        OrderOption option = new OrderOption("블랙", 3);

        OrderItem orderItem = OrderItem.of(option, 30000, order, product);

        assertThat(orderItem.getUnitPrice()).isEqualTo(30000);
        assertThat(orderItem.getQuantity()).isEqualTo(3);
        assertThat(orderItem.getTotalPrice()).isEqualTo(90000);
        assertThat(orderItem.getColor()).isEqualTo("블랙");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void 수량이_1_미만이면_InvalidOrderQuantityException(int quantity) {
        Product product = productWithColors("블랙");
        OrderOption option = new OrderOption("블랙", quantity);

        assertThatThrownBy(() -> OrderItem.of(option, 30000, order, product))
                .isInstanceOf(InvalidOrderQuantityException.class);
    }

    @Test
    void 단가와_수량의_곱이_int_범위를_넘으면_음수로_뒤집히지_않고_OrderAmountExceededException() {
        // 30000 * 143166 = 4,294,980,000 > Integer.MAX_VALUE → int 곱셈이면 음수로 뒤집힘
        Product product = productWithColors("블랙");
        OrderOption option = new OrderOption("블랙", 143166);

        assertThatThrownBy(() -> OrderItem.of(option, 30000, order, product))
                .isInstanceOf(OrderAmountExceededException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"하늘색", "", " 블랙", "블 랙"})
    void 상품이_판매하지_않는_색상이면_InvalidOrderColorException(String color) {
        Product product = productWithColors("블랙", "베이지");
        OrderOption option = new OrderOption(color, 1);

        assertThatThrownBy(() -> OrderItem.of(option, 30000, order, product))
                .isInstanceOf(InvalidOrderColorException.class);
    }

    @Test
    void 삭제된_색상으로는_주문할_수_없다() {
        Product product = productWithColors("블랙", "베이지");
        List<ProductColor> productColors = product.getColors();
        ProductColor blackColor = productColors.get(0);
        blackColor.delete();
        OrderOption option = new OrderOption("블랙", 1);

        assertThatThrownBy(() -> OrderItem.of(option, 30000, order, product))
                .isInstanceOf(InvalidOrderColorException.class);
    }

    @Test
    void 색상_옵션이_없는_상품은_빈_색상으로_주문할_수_있다() {
        Product product = productWithColors();
        OrderOption option = new OrderOption("", 1);

        OrderItem orderItem = OrderItem.of(option, 30000, order, product);

        assertThat(orderItem.getColor()).isEmpty();
    }

    @Test
    void 색상_옵션이_없는_상품에_색상을_지정하면_InvalidOrderColorException() {
        Product product = productWithColors();
        OrderOption option = new OrderOption("블랙", 1);

        assertThatThrownBy(() -> OrderItem.of(option, 30000, order, product))
                .isInstanceOf(InvalidOrderColorException.class);
    }
}
