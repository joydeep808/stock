package com.jstock.jstock.service_interface;

import java.util.List;
import java.util.UUID;

import com.jstock.jstock.dto.order.OrderBook;
import com.jstock.jstock.dto.order.OrderRequest;
import com.jstock.jstock.entity.Order;

public interface OrderInterface {

  Order placeOrder(OrderRequest request);

  Order cancelOrder(UUID orderId);

  // Order modifyOrder(UUID orderId, OrderModificationRequest request);

  List<Order> getOrdersByAccount(UUID accountId);

  OrderBook getOrderBook(String symbol);
}