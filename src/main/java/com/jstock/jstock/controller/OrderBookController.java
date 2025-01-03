package com.jstock.jstock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.jstock.jstock.dto.order.OrderBook;
import com.jstock.jstock.service.EmiterService;
import com.jstock.jstock.service.OrderBookService;
import com.jstock.jstock.util.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orderbook")

public class OrderBookController {

  private final OrderBookService orderBookService;

  @GetMapping("/{symbol}")
  public SseEmitter getOrderBook(@PathVariable String symbol) {
    return orderBookService.registerUserForGetOrderHistory(symbol);
  }

}
