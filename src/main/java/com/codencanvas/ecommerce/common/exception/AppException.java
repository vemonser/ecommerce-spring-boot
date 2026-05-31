package com.codencanvas.ecommerce.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final int status;
    private final String messageKey;

    public AppException(String messageKey, int status) {
        super(messageKey);
        this.status = status;
        this.messageKey = messageKey;
    }
}
