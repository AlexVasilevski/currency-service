package com.currency.service.client;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.http.HttpTimeoutException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.currency.service.api.CurrencyExchangeRates;

@ExtendWith(MockitoExtension.class)
public class FixerioCurrencyClientTest {

    private static final String PATH_GET_LATEST_CURRENCY_RATE = "/api/latest";
    private static final String TEST_API_ACCESS_KEY = "some_api_access_key";
    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final BigDecimal RATE = BigDecimal.valueOf(0.85);
    private static final LocalDateTime HOUR_AGO = LocalDateTime.now().minusHours(1);

    @Mock
    private RestTemplate restTemplate;

    private FixerioCurrencyClient currencyClient;

    @BeforeEach
    void setUp() {
        currencyClient = new FixerioCurrencyClient(TEST_API_ACCESS_KEY, restTemplate);
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(restTemplate);
    }

    @Test
    void shouldGetCurrencyExchangeRates() {

        Set<String> availableCurrencies = Collections.singleton(EUR);
        CurrencyExchangeRates expectedRates = new CurrencyExchangeRates(USD, HOUR_AGO, Collections.singletonMap(EUR, RATE));
        when(restTemplate.getForObject(PATH_GET_LATEST_CURRENCY_RATE, CurrencyExchangeRates.class, TEST_API_ACCESS_KEY, USD, EUR))
                .thenReturn(expectedRates);

        Optional<CurrencyExchangeRates> result = currencyClient.getCurrencyExchangeRates(USD, availableCurrencies);

        assertTrue(result.isPresent());
        assertEquals(expectedRates, result.get());
        verify(restTemplate).getForObject(PATH_GET_LATEST_CURRENCY_RATE, CurrencyExchangeRates.class, TEST_API_ACCESS_KEY, USD, EUR);
    }

    @Test
    void shouldGetCurrencyExchangeRatesWhenAvailableCurrenciesIsEmpty() {
        Set<String> availableCurrencies = Collections.emptySet();
        currencyClient.getCurrencyExchangeRates(USD, availableCurrencies);
        verify(restTemplate).getForObject(PATH_GET_LATEST_CURRENCY_RATE, CurrencyExchangeRates.class, TEST_API_ACCESS_KEY, USD, EMPTY);
    }

    @Test
    void shouldReturnEmptyWhenExceptionIsOccurred() {
        Set<String> availableCurrencies = Collections.singleton(EUR);
        when(restTemplate.getForObject(PATH_GET_LATEST_CURRENCY_RATE, CurrencyExchangeRates.class, TEST_API_ACCESS_KEY, USD, EUR))
                .thenThrow(new RuntimeException("test message"));

        Optional<CurrencyExchangeRates> result = currencyClient.getCurrencyExchangeRates(USD, availableCurrencies);

        assertFalse(result.isPresent());
        verify(restTemplate).getForObject(PATH_GET_LATEST_CURRENCY_RATE, CurrencyExchangeRates.class, TEST_API_ACCESS_KEY, USD, EUR);
    }
}