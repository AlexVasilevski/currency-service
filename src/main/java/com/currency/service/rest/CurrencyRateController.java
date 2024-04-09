package com.currency.service.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.currency.service.api.CurrencyExchangeRates;
import com.currency.service.service.CurrencyExchangeRateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Currency Rate controller", description = "Here you can get currency exchange rates")
public class CurrencyRateController {

    private final CurrencyExchangeRateService currencyExchangeRateService;

    @GetMapping("/currencies/rates/{currencyName}")
    @Cacheable(value = "currencyExchangeRateCacheName")
    public CurrencyExchangeRates getCurrencyRates(@PathVariable String currencyName) {
        log.info("Calling GET currency {}", currencyName);
        return currencyExchangeRateService.getCurrencyExchangeRates(currencyName);
    }
}
