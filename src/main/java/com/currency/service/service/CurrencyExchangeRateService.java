package com.currency.service.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.currency.service.api.CurrencyExchangeRates;
import com.currency.service.entity.CurrencyEntity;
import com.currency.service.entity.ExchangeRateEntity;
import com.currency.service.exception.CurrencyExchangeRatesNotFound;
import com.currency.service.repository.CurrencyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeRateService {

    private final CurrencyRepository currencyRepository;

    public CurrencyExchangeRates getCurrencyExchangeRates(String currencyName) {
        return currencyRepository.findByBase(currencyName)
                .map(this::buildCurrencyExchangeRate)
                .orElseThrow(() -> new CurrencyExchangeRatesNotFound(currencyName));
    }

    private CurrencyExchangeRates buildCurrencyExchangeRate(CurrencyEntity entity) {
        Set<ExchangeRateEntity> exchangeRates = entity.getExchangeRates();

        return CurrencyExchangeRates.builder()
                .withBase(entity.getBase())
                .withTimestamp(entity.getRatesUpdateTime())
                .withRates(buildRatesMap(exchangeRates))
                .build();
    }

    private Map<String, BigDecimal> buildRatesMap(Set<ExchangeRateEntity> exchangeRateEntities) {
        return exchangeRateEntities.stream()
                .collect(Collectors.toMap(ExchangeRateEntity::getCounterCurrency, ExchangeRateEntity::getRate));
    }
}
