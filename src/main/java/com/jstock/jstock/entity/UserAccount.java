package com.jstock.jstock.entity;

import com.jstock.jstock.util.DateTimeUtil;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@AllArgsConstructor
@Data
public class UserAccount {
  @Id
  private Long userId;
  private Double balance;
  private Double blockedBalance;
  private Double availableBalance;
  private Long createdAt;
  private Long updatedAt;

  public UserAccount() {
    this.balance = 0.0;
    this.blockedBalance = 0.0;
    this.availableBalance = 0.0;
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();
    this.updatedAt = DateTimeUtil.getCurrentTimeMilis();
  }

  public UserAccount(Long userId) {
    this.userId = userId;
    this.balance = 0.0;
    this.blockedBalance = 0.0;
    this.availableBalance = 0.0;
    this.createdAt = DateTimeUtil.getCurrentTimeMilis();
    this.updatedAt = DateTimeUtil.getCurrentTimeMilis();
  }
}
