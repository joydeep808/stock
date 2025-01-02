package com.jstock.jstock.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

  public static final String ORDER_QUEUE = "order_queue";
  public static final String PARTIAL_TRADE_QUEUE = "partial_success_queue";
  public static final String FULL_TRADE_SUCCESS_QUEUE = "full_success_queue";
  public static final String TRADE_QUEUE = "trade_queue";
  public static final String USER_BALANCE_QUEUE = "user_balance_queue";

  @Bean
  public Queue orderQueue() {
    return new Queue(ORDER_QUEUE);
  }

  @Bean
  public Queue partialSuccessQueue() {
    return new Queue(PARTIAL_TRADE_QUEUE);
  }

  @Bean
  public Queue fullSuccessQueue() {
    return new Queue(FULL_TRADE_SUCCESS_QUEUE);
  }

  @Bean
  public Queue tradeQueue() {
    return new Queue(TRADE_QUEUE);
  }

  @Bean
  public Queue userBalanceQueue() {
    return new Queue(USER_BALANCE_QUEUE);
  }
}
