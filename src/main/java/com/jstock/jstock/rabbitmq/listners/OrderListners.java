package com.jstock.jstock.rabbitmq.listners;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jstock.jstock.constants.Constant.OrderStatus;
import com.jstock.jstock.entity.Order;
import com.jstock.jstock.entity.Trade;
import com.jstock.jstock.entity.UserBalanceTransaction;
import com.jstock.jstock.rabbitmq.RabbitMqConfig;
import com.jstock.jstock.repository.OrderRepo;
import com.jstock.jstock.repository.TradeRepo;
import com.jstock.jstock.repository.UserBalanceTransactionRepo;
import com.jstock.jstock.service.StockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderListners {

  private final OrderRepo orderRepo;
  private final TradeRepo tradeRepo;
  private final ObjectMapper objectMapper;
  private final UserBalanceTransactionRepo transactionRepo;
  private final StockService stockService;

  @RabbitListener(queues = RabbitMqConfig.ORDER_QUEUE)
  public void handleOrder(String message) {
    try {
      Order wantsToSaveTheOrder = objectMapper.readValue(message, Order.class);

      orderRepo.save(wantsToSaveTheOrder);
      return;
    } catch (Exception e) {
      log.error("Error occurred while processing order: {}", message, e);
    }
  }

  @RabbitListener(queues = RabbitMqConfig.TRADE_QUEUE)
  public void handleTrade(String message) {
    try {
      Trade trade = objectMapper.readValue(message, Trade.class);
      tradeRepo.save(trade);
      return;
    } catch (Exception e) {
      log.error("Error occurred while processing trade: {}", message, e);
    }
  }

  @RabbitListener(queues = RabbitMqConfig.USER_BALANCE_QUEUE)
  public void handleBalanceHistory(String message) {
    try {
      UserBalanceTransaction transaction = objectMapper.readValue(message, UserBalanceTransaction.class);
      transactionRepo.save(transaction);
      return;
    } catch (Exception e) {
      log.error("Error occurred while processing transaction: {}", message, e);
    }

  }

  @RabbitListener(queues = RabbitMqConfig.PARTIAL_TRADE_QUEUE)
  public void handlePartialOrder(String message) {
    try {
      Order order = objectMapper.readValue(message, Order.class);
      Order foundOrder = orderRepo.findById(order.getId()).orElse(order);
      stockService.updateProductPrice(order.getSymbol(), order.getPrice());
      foundOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
      foundOrder.setFilledQuantity(foundOrder.getFilledQuantity() + order.getQuantity());
      orderRepo.save(foundOrder);
      return;
    } catch (Exception e) {
      log.error("Error occurred while processing transaction: {}", message, e);
    }
  }

  @RabbitListener(queues = RabbitMqConfig.FULL_TRADE_SUCCESS_QUEUE)
  public void handleFullOrder(String message) {
    try {
      Order order = objectMapper.readValue(message, Order.class);
      Order foundOrder = orderRepo.findById(order.getId()).orElse(order);
      stockService.updateProductPrice(order.getSymbol(), order.getPrice());
      foundOrder.setStatus(OrderStatus.FILLED);
      foundOrder.setFilledQuantity(foundOrder.getFilledQuantity() + order.getQuantity());
      orderRepo.save(foundOrder);
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

}
