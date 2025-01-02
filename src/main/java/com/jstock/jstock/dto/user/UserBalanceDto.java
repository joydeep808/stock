package com.jstock.jstock.dto.user;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBalanceDto {
  private Long userId;
  private Map<String, Double> balances;

  public void updateBalances(Map<String, Double> updatedBalances) {
    // Iterate over the updatedBalances map and update only existing keys
    for (Map.Entry<String, Double> entry : updatedBalances.entrySet()) {
      this.balances.put(entry.getKey(), entry.getValue());
    }
  }

}
