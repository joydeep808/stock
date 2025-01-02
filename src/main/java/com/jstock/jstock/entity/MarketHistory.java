package com.jstock.jstock.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class MarketHistory {
  @Id
  private String id;
  private String symbol;
  private String type;
  private Double price;

  public MarketHistory() {
    this.id = UUID.randomUUID().toString().substring(0, 8);
  }
}
