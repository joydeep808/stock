package com.jstock.jstock.exception.thrown_exception;

import com.jstock.jstock.exception.MainException;

public class RateLimitExceededException extends MainException {
  public RateLimitExceededException(String message) {
    super(message, 429);
  }
}
