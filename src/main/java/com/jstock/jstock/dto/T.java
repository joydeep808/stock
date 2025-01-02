package com.jstock.jstock.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
class TransactionContext {
  private Map<String, Object> originalStates = new HashMap<>();
  private boolean committed = false;

  public void storeOriginal(String key, Object value) {
    originalStates.put(key, value);
  }

  public void commit() {
    committed = true;
    originalStates.clear();
  }

  public void rollback() {
    if (!committed) {
      // Implement rollback logic here
      // Restore original states from originalStates map
    }
  }
}
