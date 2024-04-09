package com.currency.service.exception;

import static java.lang.String.format;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoSuchCurrencyExistException extends RuntimeException {
    private static final String MESSAGE = "Currency with tag: %s does not exist";

    public NoSuchCurrencyExistException(String currencyName) {
        super(format(MESSAGE, currencyName));
    }
}
