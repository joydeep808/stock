package com.jstock.jstock.exception.thrown_exception;

import com.jstock.jstock.exception.MainException;

public class GenericException extends MainException {
  public GenericException(String message, Integer code) {
    super(message, code);
  }
}
