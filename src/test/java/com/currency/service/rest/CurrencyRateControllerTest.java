package com.currency.service.rest;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.currency.service.api.CurrencyExchangeRates;
import com.currency.service.service.CurrencyExchangeRateService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CurrencyRateController.class)
public class CurrencyRateControllerTest {

    private static final String PATH_GET_CURRENCY_RATES = "/currencies/rates/USD";
    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final BigDecimal RATE = BigDecimal.valueOf(0.85);
    private static final LocalDateTime HOUR_AGO = LocalDateTime.now().minusHours(1);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyExchangeRateService currencyExchangeRateService;

    @Test
    void testGetCurrencyRates() throws Exception {
        CurrencyExchangeRates mockExchangeRates = new CurrencyExchangeRates(USD, HOUR_AGO, singletonMap(EUR, RATE));
        when(currencyExchangeRateService.getCurrencyExchangeRates(USD)).thenReturn(mockExchangeRates);

        mockMvc.perform(get(PATH_GET_CURRENCY_RATES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value(USD))
                .andExpect(jsonPath("$.rates.EUR").value(0.85));

        verify(currencyExchangeRateService, times(1)).getCurrencyExchangeRates(USD);
    }
}