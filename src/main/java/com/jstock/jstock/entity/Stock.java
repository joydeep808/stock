package com.jstock.jstock.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class Stock {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Long id;
  private String symbol;
  private String name;
  private String description;
  private Double price;
  private Long createdAt;
  private Long updatedAt;
  private String company_name;
  private Double face_value;
  private Double market_cap;
  private Long total_shares;

  public Stock() {
    this.createdAt = ZonedDateTime.now().toInstant().toEpochMilli();
    this.updatedAt = ZonedDateTime.now().toInstant().toEpochMilli();
  }

  public Stock(String symbol, String name, String description, Double price, String company_name, Double face_value,
      Double market_cap, Long total_shares) {
    this.symbol = symbol;
    this.name = name;
    this.description = description;
    this.price = price;
    this.company_name = company_name;
    this.face_value = face_value;
    this.market_cap = market_cap;
    this.total_shares = total_shares;
    this.createdAt = ZonedDateTime.now().toInstant().toEpochMilli();
    this.updatedAt = ZonedDateTime.now().toInstant().toEpochMilli();
  }

}