package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.order.exception.OrderAmountExceededException;

/**
 * 주문 금액 연산. int 범위를 넘으면 음수로 뒤집히는 대신 OrderAmountExceededException(400)을 던진다.
 */
public final class OrderAmounts {

    private OrderAmounts() {
    }

    public static int multiply(int unitPrice, int quantity) {
        try {
            return Math.multiplyExact(unitPrice, quantity);
        } catch (ArithmeticException e) {
            throw OrderAmountExceededException.EXCEPTION;
        }
    }

    public static int add(int augend, int addend) {
        try {
            return Math.addExact(augend, addend);
        } catch (ArithmeticException e) {
            throw OrderAmountExceededException.EXCEPTION;
        }
    }
}
