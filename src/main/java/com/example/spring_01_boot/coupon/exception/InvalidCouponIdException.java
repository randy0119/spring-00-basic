package com.example.spring_01_boot.coupon.exception;

/**
 * {@code c-{id}} 형식이 아니거나 비어 있는 경우.
 */
public class InvalidCouponIdException extends RuntimeException {

    public InvalidCouponIdException(String message) {
        super(message);
    }
}
