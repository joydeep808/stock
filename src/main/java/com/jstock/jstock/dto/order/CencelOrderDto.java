package com.jstock.jstock.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CencelOrderDto {
  private String orderId;
  private String symbol;
  private Double quantity;
  private boolean isSuccess = false;
  private String side;
  private Double price;

}
