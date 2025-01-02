package com.jstock.jstock.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jstock.jstock.constants.Constant;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

  @Bean
  public Queue orderQueue() {
    return new Queue(Constant.ORDER_QUEUE);
  }

  @Bean
  public Queue partialSuccessQueue() {
    return new Queue(Constant.PARTIAL_TRADE_QUEUE);
  }

  @Bean
  public Queue fullSuccessQueue() {
    return new Queue(Constant.FULL_TRADE_SUCCESS_QUEUE);
  }

  @Bean
  public Queue tradeQueue() {
    return new Queue(Constant.TRADE_QUEUE);
  }

  @Bean
  public Queue userBalanceQueue() {
    return new Queue(Constant.USER_BALANCE_QUEUE);
  }

  @Bean
  public Queue updateSentQueue() {
    return new Queue(Constant.UPDATE_SENT_QUEUE);
  }
}
