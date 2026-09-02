package com.acme.orders.module;

/**
 * Stand-in for the pricing logic a real "ProcessOrder.bwp" would implement
 * (e.g. a Mapper/Business Rule activity). Exists so the Maven build and test
 * stages have something real to compile, test, and package.
 */
public class OrderCalculator {

    public double calculateTotal(double subtotal, double discountRate) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("subtotal must not be negative");
        }
        if (discountRate < 0 || discountRate > 1) {
            throw new IllegalArgumentException("discountRate must be between 0 and 1");
        }
        return subtotal * (1 - discountRate);
    }
}
