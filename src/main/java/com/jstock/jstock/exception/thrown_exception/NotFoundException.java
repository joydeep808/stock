package com.jstock.jstock.exception.thrown_exception;

import com.jstock.jstock.exception.MainException;

public class NotFoundException extends MainException {
  public NotFoundException(String message) {
    super(message, 404);
  }
}
