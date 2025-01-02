package com.jstock.jstock.exception.thrown_exception;

import com.jstock.jstock.exception.MainException;

public class AlreadyExistException extends MainException {
  public AlreadyExistException(String message) {
    super(message, 400);
  }

}
