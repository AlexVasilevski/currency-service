package com.currency.service.client;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.currency.service.api.CurrencyExchangeRates;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FixerioCurrencyClient {

    private static final String PATH_GET_LATEST_CURRENCY_RATE = "/api/latest";

    private final String accessKey;
    private final RestTemplate fixerRestTemplate;

    public FixerioCurrencyClient(@Value("${service.fixer-io-api-key}") String accessKey, RestTemplate fixerRestTemplate) {
        this.accessKey = accessKey;
        this.fixerRestTemplate = fixerRestTemplate;
    }

    public Optional<CurrencyExchangeRates> getCurrencyExchangeRates(String base, Set<String> availableCurrencies) {
        log.info("Calling {} to get latest rates for {}", PATH_GET_LATEST_CURRENCY_RATE, base);
        try {
            String symbols = "";
            if (!availableCurrencies.isEmpty()) {
                symbols = String.join(",", availableCurrencies);
            }

            CurrencyExchangeRates object = fixerRestTemplate.getForObject(PATH_GET_LATEST_CURRENCY_RATE, CurrencyExchangeRates.class, accessKey, base, symbols);
            log.info("Received object {}", object);
            return Optional.of(object);
        } catch (HttpStatusCodeException e) {
            log.error("Error occurred while trying to get latest rates for {}" + base, e);
            return Optional.empty();
        }
    }
}
