package com.jstock.jstock.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
  @NotNull(message = "Symbol is required")
  private String symbol;
  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be positive")
  private Double quantity;
  @NotNull(message = "Price is required")
  @Positive(message = "Price must be positive")
  private Double price;
  @NotNull(message = "Type is required")
  private String type;
  private Long userId;
}