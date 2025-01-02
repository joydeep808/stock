package com.jstock.jstock.util;

import org.springframework.http.ResponseEntity;

import lombok.Data;

@Data
public class Response<T> {

  private String message;
  private Integer statusCode;
  private T data;

  public static <T> ResponseEntity<Response<T>> sendSuccessResponse(String message, T data) {
    Response<T> response = new Response<>();
    response.message = message;
    response.data = data;
    response.statusCode = 200;
    return ResponseEntity.status(200).body(response);
  }

  public static ResponseEntity<Response<String>> sendSuccessResponse(String message) {
    Response<String> response = new Response<>();
    response.message = message;
    response.statusCode = 200;
    return ResponseEntity.status(200).body(response);
  }

  public static <T> ResponseEntity<Response<T>> sendErrorResponse(String message, Integer statusCode) {
    Response<T> response = new Response<>();
    response.message = message;
    response.statusCode = statusCode;
    return ResponseEntity.status(statusCode).body(response);

  }
}
