package com.codencanvas.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OAuth2ProcessingException extends RuntimeException {
    public OAuth2ProcessingException(String message) {
        super(message);
    }
}
