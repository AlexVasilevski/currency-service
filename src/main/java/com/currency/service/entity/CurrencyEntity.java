package com.currency.service.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "currency")
public class CurrencyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(unique=true, name = "base")
    private String base;

    @UpdateTimestamp
    @Column(name = "rates_update_time")
    private LocalDateTime ratesUpdateTime;

    @Setter
    @OneToMany
    private Set<ExchangeRateEntity> exchangeRates;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyEntity that = (CurrencyEntity) o;
        return base.equals(that.base) && ratesUpdateTime.equals(that.ratesUpdateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, ratesUpdateTime);
    }
}
