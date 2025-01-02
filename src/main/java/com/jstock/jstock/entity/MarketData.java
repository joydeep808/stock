package com.jstock.jstock.entity;

import java.util.UUID;

import com.jstock.jstock.util.DateTimeUtil;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "market_data")
@Data
@AllArgsConstructor
public class MarketData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String stock;

    private Long tradeDate;

    private Double openPrice;

    private Double highPrice;

    private Double lowPrice;

    private Double closePrice;

    private Double prevClose;

    private Long volume;

    private Long tradedValue;

    private Long createdAt;

    public MarketData() {
        this.createdAt = DateTimeUtil.getCurrentTimeMilis();
    }

}
