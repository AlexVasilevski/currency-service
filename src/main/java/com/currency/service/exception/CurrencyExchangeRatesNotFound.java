package com.currency.service.exception;

import static java.lang.String.format;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CurrencyExchangeRatesNotFound extends RuntimeException {
    private static final String MESSAGE = "Can't found exchange rates for %s currency";

    public CurrencyExchangeRatesNotFound(String currencyName) {
        super(format(MESSAGE, currencyName));
    }
}