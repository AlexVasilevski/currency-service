package com.currency.service.rest;

import java.util.Set;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.currency.service.api.Currency;
import com.currency.service.service.CurrencyService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Currency controller", description = "Here you can add currency and get previously added currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/currencies")
    @Cacheable(value = "availableCurrenciesCacheName")
    public Set<Currency> getCurrencies() {
        log.info("Calling GET all currencies");
        return currencyService.getCurrencies();
    }

    @PostMapping("/currencies")
    @ResponseStatus(HttpStatus.CREATED)
    @CachePut(value = "availableCurrenciesCacheName")
    @CacheEvict(value = "currencyExchangeRateCacheName")
    public void addCurrency(@RequestBody Currency currency) {
        log.info("Calling POST currency {}", currency.getBase());
        currencyService.addCurrency(currency);
    }
}
