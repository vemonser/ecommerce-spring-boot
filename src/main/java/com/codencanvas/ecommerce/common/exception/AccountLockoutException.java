package com.codencanvas.ecommerce.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.LOCKED)
public class AccountLockoutException extends RuntimeException {

    private final long remainingMinutes;

    public AccountLockoutException(String message, long remainingMinutes) {
        super(message);
        this.remainingMinutes = remainingMinutes;
    }

    public long getRemainingMinutes() {
        return remainingMinutes;
    }
}
