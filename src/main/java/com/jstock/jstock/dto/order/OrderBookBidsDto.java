package com.jstock.jstock.dto.order;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderBookBidsDto {
  private String id;
  private String symbol;
  private Long userId;
  private Double quantity;
  private Double price;

  public OrderBookBidsDto(String symbol, Long userId, Double quantity, Double price) {
    this.price = price;
    this.quantity = quantity;
    this.userId = userId;
    this.symbol = symbol;
    this.id = UUID.randomUUID().toString().substring(0, 8);
  }

}
