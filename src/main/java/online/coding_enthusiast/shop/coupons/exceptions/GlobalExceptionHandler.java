package online.coding_enthusiast.shop.coupons.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponExhaustedException.class)
    public ResponseEntity<?> handleCouponExpired
            (CouponExhaustedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                "The coupon has no remaining usages."
        );
    }

    @ExceptionHandler(CouponAlreadyUsedException.class)
    public ResponseEntity<?> handleCouponAlreadyUsed(CouponAlreadyUsedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                "The user has already used the coupon."
        );
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<?> handleCouponNotFound(CouponNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Coupon with given code not found."
        );
    }

    @ExceptionHandler(CouponUnavailableForUserCountryException.class)
    public ResponseEntity<?> handleCountryNotAllowed(CouponUnavailableForUserCountryException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT). body(
                "The coupon cannot be used from user's country."
        );
    }

    @ExceptionHandler(CouponAlreadyExistsException.class)
    public ResponseEntity<?> handleCouponAlreadyExists(CouponAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT). body(
                "Coupon with given code already exists."
        );
    }

    @ExceptionHandler(UndeterminedUserCountryException.class)
    public ResponseEntity<?> handleUndeterminedUserCountry(UndeterminedUserCountryException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR). body(
                "Failed to determine user country."
        );
    }

    @ExceptionHandler(CountryUnsupportedException.class)
    public ResponseEntity<?> handleCountryUnsupported(CountryUnsupportedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT). body(
                "Provided country is not supported by this instance of the service."
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException  e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST). body(
                "Request malformed."
        );}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleRuntimeException(Exception e) {
        log.error("Application error ", e); // TODO: remove this logging
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "Application error."
        );
    }
}
