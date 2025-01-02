package com.jstock.jstock.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MainException extends Exception {
  private String message;
  private Integer statusCode;

  public MainException(String message, Integer code) {
    super(message);
    this.message = message;
    this.statusCode = code;
  }
}
