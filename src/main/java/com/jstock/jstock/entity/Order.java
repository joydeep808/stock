package com.jstock.jstock.entity;

import java.util.UUID;

import com.jstock.jstock.constants.Constant.OrderSide;
import com.jstock.jstock.constants.Constant.OrderStatus;
import com.jstock.jstock.constants.Constant.OrderType;
import com.jstock.jstock.util.DateTimeUtil;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@AllArgsConstructor
@Data
@Table(name = "orders")
public class Order {

  @Id
  private String id;
  private Long userId;
  private String symbol;
  private Double quantity;
  private Double filledQuantity;
  private Double price;
  @Enumerated(EnumType.STRING)
  private OrderSide side;
  @Enumerated(EnumType.STRING)
  private OrderStatus status;
  @Enumerated(EnumType.STRING)
  private OrderType type;
  private Long createdAt;
  private Long updatedAt;

  public Order() {
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();
    this.updatedAt = DateTimeUtil.getCurrentTimeMilis();
  }

  public Order(Long userId, String symbol, Double quantity, Double price, OrderSide side, OrderType type,
      OrderStatus status) {
    this.userId = userId;
    this.symbol = symbol;
    this.quantity = quantity;
    this.status = status;
    this.price = price;
    this.side = side;
    this.type = type;
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();
    this.updatedAt = DateTimeUtil.getCurrentTimeMilis();
    this.id = UUID.randomUUID().toString().substring(0, 8);
  }

  public Order(Long userId, String symbol, Double quantity, Double price, OrderSide side, OrderType type,
      OrderStatus status, String orderId) {
    this.userId = userId;
    this.symbol = symbol;
    this.quantity = quantity;
    this.status = status;
    this.price = price;
    this.side = side;
    this.type = type;
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();
    this.updatedAt = DateTimeUtil.getCurrentTimeMilis();
    this.id = UUID.randomUUID().toString().substring(0, 8);
  }

}