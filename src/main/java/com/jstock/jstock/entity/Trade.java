package com.jstock.jstock.entity;

import java.util.UUID;

import com.jstock.jstock.util.DateTimeUtil;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@AllArgsConstructor
@Data
public class Trade {
  @Id
  private String id;
  private String orderId;
  private Double quantity;

  private Long sellerId;
  @Column(nullable = false)
  private Double price;
  @Column(nullable = false)
  private Double tradeValue;
  private Long executedAt;
  private Long createdAt;
  private Long updatedAt;

  public Trade() {
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();
    this.updatedAt = DateTimeUtil.getCurrentTimeMilis();
    this.id = UUID.randomUUID().toString().substring(0, 8);
  }

  public Trade(String orderId, Double quantity, Long sellerId, Double price, Double tradeValue) {
    this.orderId = orderId;
    this.quantity = quantity;
    this.sellerId = sellerId;
    this.price = price;
    this.tradeValue = tradeValue;
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();
    this.updatedAt = DateTimeUtil.getCurrentTimeMilis();
    this.id = UUID.randomUUID().toString().substring(0, 8);
  }

}