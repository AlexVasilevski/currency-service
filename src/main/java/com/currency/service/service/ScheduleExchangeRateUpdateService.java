package com.currency.service.service;

import com.currency.service.api.CurrencyExchangeRates;
import com.currency.service.client.FixerioCurrencyClient;
import com.currency.service.entity.CurrencyEntity;
import com.currency.service.entity.ExchangeRateEntity;
import com.currency.service.exception.NoSuchCurrencyExistException;
import com.currency.service.repository.CurrencyRepository;
import com.currency.service.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduleExchangeRateUpdateService {


    private final String currencyExchangeRateCacheName;

    private final CacheManager cacheManager;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final FixerioCurrencyClient fixerioCurrencyClient;

    public ScheduleExchangeRateUpdateService(@Value("${service.currency-exchange-rate-cache}")
                                             String currencyExchangeRateCacheName,
                                             CacheManager cacheManager,
                                             CurrencyRepository currencyRepository,
                                             ExchangeRateRepository exchangeRateRepository,
                                             FixerioCurrencyClient fixerioCurrencyClient) {
        this.currencyExchangeRateCacheName = currencyExchangeRateCacheName;
        this.cacheManager = cacheManager;
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.fixerioCurrencyClient = fixerioCurrencyClient;
    }

    @Scheduled(fixedRate = 3600000)
    public void updateExchangeRates() {
        long currencyAmount = currencyRepository.count();
        if (currencyAmount >= 2) {
            Cache currencyExchangeRateCache = cacheManager.getCache(currencyExchangeRateCacheName);
            currencyExchangeRateCache.invalidate();
            log.info("Currency exchange rate cache is cleared");

            List<CurrencyEntity> currencyEntities = currencyRepository.findAll();
            Set<String> counterCurrencies = currencyEntities.stream()
                    .map(CurrencyEntity::getBase)
                    .collect(Collectors.toSet());

            log.info("Starting update of exchange rates");

            for (CurrencyEntity currencyEntity : currencyEntities) {
                Set<ExchangeRateEntity> oldExchangeRates = currencyEntity.getExchangeRates();

                CurrencyExchangeRates currencyExchangeRates =
                        fixerioCurrencyClient.getCurrencyExchangeRates(currencyEntity.getBase(), counterCurrencies)
                                .orElseThrow(() -> new NoSuchCurrencyExistException(currencyEntity.getBase()));

                exchangeRateRepository.deleteAll(oldExchangeRates);
                log.info("Old exchange rates are deleted");

                currencyExchangeRateCache.put(currencyExchangeRates.getBase(), currencyExchangeRates);

                Set<ExchangeRateEntity> newExchangeRatesEntities = buildExchangeRatesEntities(currencyExchangeRates);
                currencyEntity.setExchangeRates(newExchangeRatesEntities);
                log.info("New exchange rates are saved");
            }
        }
    }

    private Set<ExchangeRateEntity> buildExchangeRatesEntities(CurrencyExchangeRates currencyExchangeRates) {
        Map<String, BigDecimal> rates = currencyExchangeRates.getRates();

        Set<ExchangeRateEntity> newExchangeRates = new HashSet<>();
        for (String counterCurrency : rates.keySet()) {
            BigDecimal rate = rates.get(counterCurrency);

            ExchangeRateEntity newExchangeRateEntity = ExchangeRateEntity.builder()
                    .rate(rate)
                    .baseCurrency(currencyExchangeRates.getBase())
                    .counterCurrency(counterCurrency)
                    .build();

            newExchangeRates.add(newExchangeRateEntity);
        }

        exchangeRateRepository.saveAll(newExchangeRates);

        return newExchangeRates;
    }
}
