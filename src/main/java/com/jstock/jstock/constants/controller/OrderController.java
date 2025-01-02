package com.jstock.jstock.constants.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jstock.jstock.dto.order.OrderBook;
import com.jstock.jstock.dto.order.OrderRequest;
import com.jstock.jstock.entity.Order;
import com.jstock.jstock.entity.Trade;
import com.jstock.jstock.service.OrderService;
import com.jstock.jstock.util.Response;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
  private final OrderService orderService;

  @PostMapping("/place")
  public ResponseEntity<Response<String>>

      placeOrder(@RequestBody OrderRequest orderRequest) {
    return orderService.placeOrder(orderRequest);
  }

  @GetMapping("/book")
  public ResponseEntity<Response<OrderBook>> getOrderBook(@RequestParam String symbol) {
    return orderService.getOrderBook(symbol);
  }

  @GetMapping("/trades")
  public ResponseEntity<Response<List<Trade>>> getTradesByOrderId(@RequestParam String orderId) {
    return orderService.getTradesByOrderId(orderId);
  }

  @GetMapping("/user")
  public ResponseEntity<Response<Page<Order>>> getOrdersForUser(HttpServletRequest request, @RequestParam Integer page,
      @RequestParam Long userId) {
    return orderService.getOrdersForUser(request, page, userId);
  }

}
