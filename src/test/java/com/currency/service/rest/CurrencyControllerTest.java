package com.currency.service.rest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.currency.service.api.Currency;
import com.currency.service.service.CurrencyService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTest {

    private static final String PATH_GET_ALL_CURRENCIES = "/currencies";
    private static final String PATH_POST_CURRENCy = "/currencies";
    private static final String EUR_CURRENCY_JSON = "{\"base\":\"EUR\"}";
    private static final String USD = "USD";
    private static final String EUR = "EUR";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @MockBean
    private CacheManager cacheManager;

    @Test
    void shouldGetAllCurrencies() throws Exception {
        Set<Currency> currencies = Set.of(new Currency(USD));
        when(currencyService.getCurrencies()).thenReturn(currencies);

        mockMvc.perform(get(PATH_GET_ALL_CURRENCIES)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].base").value(USD));
    }

    @Test
    void shouldAddCurrency() throws Exception {
        Currency currency = new Currency(EUR);
        mockMvc.perform(post(PATH_POST_CURRENCy)
                        .content(EUR_CURRENCY_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(currencyService, times(1)).addCurrency(currency);
    }
}