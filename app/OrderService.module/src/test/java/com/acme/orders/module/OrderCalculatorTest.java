package com.acme.orders.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void appliesDiscountRate() {
        assertEquals(90.0, calculator.calculateTotal(100.0, 0.1), 0.0001);
    }

    @Test
    void zeroDiscountReturnsSubtotal() {
        assertEquals(100.0, calculator.calculateTotal(100.0, 0.0), 0.0001);
    }

    @Test
    void rejectsNegativeSubtotal() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTotal(-1.0, 0.1));
    }

    @Test
    void rejectsOutOfRangeDiscount() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateTotal(100.0, 1.5));
    }
}
