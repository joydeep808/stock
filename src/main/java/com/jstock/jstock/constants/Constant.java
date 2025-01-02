package com.jstock.jstock.constants;

public class Constant {
  public static final String SUCCESS = "success";
  public static final String ERROR = "error";
  public static final String USER_ID = "userId";
  public static final String ORDER_ID = "orderId";
  public static final String QUANTITY = "quantity";
  public static final String PRICE = "price";
  public static final String SYMBOL = "symbol";
  public static final String TYPE = "type";
  public static final String SIDE = "side";
  public static final String STATUS = "status";
  public static final String BID = "BID";
  public static final String ASKS = "ASKS";
  public static final String INR = "INR";

  public enum OrderStatus {
    PENDING,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED,
    EXPIRED
  }

  public enum OrderSide {
    BUY,
    SELL
  }

  public enum OrderType {
    MARKET,
    LIMIT,
    IMMEDIATE_OR_CANCEL,

  }
}