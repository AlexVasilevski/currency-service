package com.currency.service.service;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.currency.service.api.Currency;
import com.currency.service.api.CurrencyExchangeRates;
import com.currency.service.client.FixerioCurrencyClient;
import com.currency.service.entity.CurrencyEntity;
import com.currency.service.exception.NoSuchCurrencyExistException;
import com.currency.service.repository.CurrencyRepository;
import com.currency.service.repository.ExchangeRateRepository;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final LocalDateTime HOUR_AGO = LocalDateTime.now().minusHours(1);

    @Mock
    private FixerioCurrencyClient fixerioCurrencyClient;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(currencyRepository, fixerioCurrencyClient, exchangeRateRepository);
    }

    @Test
    void shouldGetCurrencies() {
        List<CurrencyEntity> currencyEntities = new ArrayList<>();
        currencyEntities.add(CurrencyEntity.builder().base(USD).build());
        currencyEntities.add(CurrencyEntity.builder().base(EUR).build());
        when(currencyRepository.findAll()).thenReturn(currencyEntities);

        Set<Currency> result = currencyService.getCurrencies();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getBase().equals(USD)));
        assertTrue(result.stream().anyMatch(c -> c.getBase().equals(EUR)));
    }

    @Test
    void shouldAddCurrencyWhenNoCurrenciesIsPresent() {
        Currency currency = new Currency(USD);
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

        currencyService.addCurrency(currency);
        verify(currencyRepository).save(any(CurrencyEntity.class));
    }

    @Test
    void shouldAddCurrencyWhenCurrencyDoesNotAdded() {
        Currency currency = new Currency(USD);
        CurrencyExchangeRates exchangeRates = new CurrencyExchangeRates(USD, HOUR_AGO, Collections.singletonMap(EUR, BigDecimal.valueOf(0.85)));

        when(currencyRepository.findAll()).thenReturn(singletonList(CurrencyEntity.builder().base(EUR).build()));
        when(currencyRepository.existsByBase(USD)).thenReturn(false);
        when(exchangeRateRepository.saveAll(anySet())).thenReturn(singletonList(null));
        when(fixerioCurrencyClient.getCurrencyExchangeRates(USD, singleton(EUR))).thenReturn(Optional.of(exchangeRates));

        currencyService.addCurrency(currency);

        verify(currencyRepository).save(any(CurrencyEntity.class));
    }

    @Test
    void shouldNotAddCurrencyWhenCurrencyAlreadyAdded() {
        Currency currency = new Currency(USD);

        when(currencyRepository.findAll()).thenReturn(singletonList(CurrencyEntity.builder().base(USD).build()));
        when(currencyRepository.existsByBase(USD)).thenReturn(true);
        currencyService.addCurrency(currency);
        verify(currencyRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCurrencyDoesNotExist() {
        Currency currency = new Currency(USD);

        when(currencyRepository.findAll()).thenReturn(singletonList(CurrencyEntity.builder().base(EUR).build()));
        when(currencyRepository.existsByBase(USD)).thenReturn(false);
        when(fixerioCurrencyClient.getCurrencyExchangeRates(USD, singleton(EUR))).thenReturn(Optional.empty());

        assertThrows(NoSuchCurrencyExistException.class, () -> currencyService.addCurrency(currency));
    }
}
