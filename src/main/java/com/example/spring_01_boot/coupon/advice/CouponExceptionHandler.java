package com.example.spring_01_boot.coupon.advice;

import com.example.spring_01_boot.coupon.dto.CouponErrorResponse;
import com.example.spring_01_boot.coupon.exception.CouponIssueConflictException;
import com.example.spring_01_boot.coupon.exception.CouponNotFoundException;
import com.example.spring_01_boot.coupon.exception.InvalidCouponIdException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = com.example.spring_01_boot.coupon.controller.couponController.class)
public class CouponExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<CouponErrorResponse> notFound(CouponNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new CouponErrorResponse("COUPON_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(CouponIssueConflictException.class)
    public ResponseEntity<CouponErrorResponse> conflict(CouponIssueConflictException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new CouponErrorResponse(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(InvalidCouponIdException.class)
    public ResponseEntity<CouponErrorResponse> badCouponId(InvalidCouponIdException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new CouponErrorResponse("INVALID_COUPON_ID", e.getMessage()));
    }
}
