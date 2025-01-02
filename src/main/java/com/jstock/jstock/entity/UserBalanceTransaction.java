package com.jstock.jstock.entity;

import com.jstock.jstock.util.DateTimeUtil;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@Data

public class UserBalanceTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private Long userId;
  private Double amount;
  private String symbol;
  @Enumerated(EnumType.STRING)
  private TransactionType type;
  private Boolean isCredited;
  private Long createdAt;

  public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    BID,
    ASKS
  }

  public UserBalanceTransaction() {
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();

  }

  public UserBalanceTransaction(Long userId, Double amount, TransactionType type, Boolean isCredited) {
    this.userId = userId;
    this.amount = amount;
    this.type = type;
    this.isCredited = isCredited;
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();
  }

  public UserBalanceTransaction(Long userId, Double amount, TransactionType type, Boolean isCredited, String symbol) {
    this.userId = userId;
    this.amount = amount;
    this.type = type;
    this.isCredited = isCredited;
    this.symbol = symbol;
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();

  }

}
