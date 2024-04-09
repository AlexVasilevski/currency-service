package com.currency.service.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.currency.service.api.Currency;
import com.currency.service.api.CurrencyExchangeRates;
import com.currency.service.client.FixerioCurrencyClient;
import com.currency.service.entity.CurrencyEntity;
import com.currency.service.entity.ExchangeRateEntity;
import com.currency.service.exception.NoSuchCurrencyExistException;
import com.currency.service.repository.CurrencyRepository;
import com.currency.service.repository.ExchangeRateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final FixerioCurrencyClient fixerioCurrencyClient;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public Set<Currency> getCurrencies() {
        return currencyRepository.findAll()
                .stream()
                .map(this::buildCurrency)
                .collect(Collectors.toSet());
    }

    public void addCurrency(Currency currency) {
        List<CurrencyEntity> availableCurrencies = currencyRepository.findAll();

        if (availableCurrencies.isEmpty()) {
            currencyRepository.save(buildCurrencyEntity(currency));
            return;
        }

        Set<String> counterCurrencies = availableCurrencies.stream()
                .map(CurrencyEntity::getBase)
                .collect(Collectors.toSet());

        if (!currencyRepository.existsByBase(currency.getBase())) {
            CurrencyExchangeRates exchangeRates =
                    fixerioCurrencyClient.getCurrencyExchangeRates(currency.getBase(), counterCurrencies)
                    .orElseThrow(() -> new NoSuchCurrencyExistException(currency.getBase()));

            CurrencyEntity currencyEntity = buildCurrencyEntity(exchangeRates);
            currencyRepository.save(currencyEntity);
        }
    }

    private CurrencyEntity buildCurrencyEntity(CurrencyExchangeRates exchangeRates) {
        Map<String, BigDecimal> rates = exchangeRates.getRates();

        Set<ExchangeRateEntity> exchangeRatesSet = new HashSet<>();
        for (String counterCurrency : rates.keySet()) {
            BigDecimal rate = rates.get(counterCurrency);

            ExchangeRateEntity entity = ExchangeRateEntity.builder()
                    .baseCurrency(exchangeRates.getBase())
                    .counterCurrency(counterCurrency)
                    .rate(rate)
                    .build();

            exchangeRatesSet.add(entity);
        }

        exchangeRateRepository.saveAll(exchangeRatesSet);

        return CurrencyEntity.builder()
                .base(exchangeRates.getBase())
                .exchangeRates(exchangeRatesSet)
                .build();
    }

    private CurrencyEntity buildCurrencyEntity(Currency currency) {
        return CurrencyEntity.builder()
                .base(currency.getBase())
                .build();
    }

    private Currency buildCurrency(CurrencyEntity currencyEntity) {
        return Currency.builder()
                .base(currencyEntity.getBase())
                .build();
    }
}
