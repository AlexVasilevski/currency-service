package com.currency.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.currency.service.api.CurrencyExchangeRates;
import com.currency.service.entity.CurrencyEntity;
import com.currency.service.entity.ExchangeRateEntity;
import com.currency.service.exception.CurrencyExchangeRatesNotFound;
import com.currency.service.repository.CurrencyRepository;

@ExtendWith(MockitoExtension.class)
public class CurrencyExchangeRateServiceTest {
    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final BigDecimal RATE = BigDecimal.valueOf(0.85);

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyExchangeRateService currencyExchangeRateService;

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    void shouldGetCurrencyExchangeRatesWhenCurrencyExists() {

        ExchangeRateEntity exchangeRateEntity = new ExchangeRateEntity(null, USD, EUR, RATE);
        CurrencyEntity currencyEntity =
                new CurrencyEntity(null, USD, LocalDateTime.now(), Collections.singleton(exchangeRateEntity));
        when(currencyRepository.findByBase(USD)).thenReturn(Optional.of(currencyEntity));

        CurrencyExchangeRates result = currencyExchangeRateService.getCurrencyExchangeRates(USD);

        assertNotNull(result);
        assertEquals(USD, result.getBase());
        assertTrue(result.getRates().containsKey(EUR));
        assertEquals(RATE, result.getRates().get(EUR));
    }

    @Test
    void testGetCurrencyExchangeRates_WhenCurrencyDoesNotExist() {
        when(currencyRepository.findByBase(USD)).thenReturn(Optional.empty());
        assertThrows(CurrencyExchangeRatesNotFound.class, () -> currencyExchangeRateService.getCurrencyExchangeRates(USD));
    }
}
