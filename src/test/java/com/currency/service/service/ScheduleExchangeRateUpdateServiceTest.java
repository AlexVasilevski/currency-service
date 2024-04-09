package com.currency.service.service;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.currency.service.api.CurrencyExchangeRates;
import com.currency.service.client.FixerioCurrencyClient;
import com.currency.service.entity.CurrencyEntity;
import com.currency.service.exception.NoSuchCurrencyExistException;
import com.currency.service.repository.CurrencyRepository;
import com.currency.service.repository.ExchangeRateRepository;

@ExtendWith(MockitoExtension.class)
public class ScheduleExchangeRateUpdateServiceTest {

    private static final String CACHE_NAME = "currencyExchangeRateCache";
    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final BigDecimal RATE = BigDecimal.valueOf(0.85);
    private static final LocalDateTime HOUR_AGO = LocalDateTime.now().minusHours(1);

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache currencyExchangeRateCache;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private FixerioCurrencyClient fixerioCurrencyClient;

    @InjectMocks
    private ScheduleExchangeRateUpdateService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleExchangeRateUpdateService(CACHE_NAME, cacheManager, currencyRepository, exchangeRateRepository, fixerioCurrencyClient);
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(cacheManager, currencyRepository, exchangeRateRepository, fixerioCurrencyClient);
    }

    @Test
    void shouldNotUpdateExchangeRatesWhenCurrencyAmountIsLessThanTwo() {
        when(currencyRepository.count()).thenReturn(1L);
        scheduleService.updateExchangeRates();
        verify(currencyRepository, never()).findAll();
    }

    @Test
    void shouldUpdateExchangeRatesWhenCurrencyAmountIsTwoOrMore() {
        CurrencyEntity currencyEntity1 = new CurrencyEntity(null, USD, null, null);
        CurrencyEntity currencyEntity2 = new CurrencyEntity(null, EUR, null, null);
        CurrencyExchangeRates exchangeRates = new CurrencyExchangeRates(USD, HOUR_AGO, Collections.singletonMap(EUR, RATE));

        when(cacheManager.getCache(CACHE_NAME)).thenReturn(currencyExchangeRateCache);
        when(fixerioCurrencyClient.getCurrencyExchangeRates(USD, Set.of(EUR, USD))).thenReturn(Optional.of(exchangeRates));
        when(fixerioCurrencyClient.getCurrencyExchangeRates(EUR, Set.of(USD, EUR))).thenReturn(Optional.of(exchangeRates));
        when(currencyRepository.count()).thenReturn(2L);
        when(currencyRepository.findAll()).thenReturn(List.of(currencyEntity1, currencyEntity2));

        scheduleService.updateExchangeRates();

        verify(currencyExchangeRateCache).invalidate();
        verify(currencyRepository).findAll();
        verify(exchangeRateRepository, times(2)).deleteAll(any());
        verify(exchangeRateRepository, times(2)).saveAll(any());
        verify(currencyExchangeRateCache, times(2)).put(eq(USD), any());
    }

    @Test
    void shouldThrowExceptionWhenSuchCurrencyDoesNotExist() {
        CurrencyEntity currencyEntity1 = new CurrencyEntity(null, USD, null, null);
        CurrencyEntity currencyEntity2 = new CurrencyEntity(null, EUR, null, null);
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(currencyExchangeRateCache);
        when(fixerioCurrencyClient.getCurrencyExchangeRates(USD, Set.of(EUR, USD))).thenReturn(Optional.empty());
        when(currencyRepository.count()).thenReturn(2L);
        when(currencyRepository.findAll()).thenReturn(List.of(currencyEntity1, currencyEntity2));

        assertThrows(NoSuchCurrencyExistException.class, () -> scheduleService.updateExchangeRates());
    }
}
