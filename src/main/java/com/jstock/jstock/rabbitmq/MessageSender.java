package com.jstock.jstock.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageSender {

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public <T> void send(String route, T message) {
    try {
      rabbitTemplate.convertAndSend(route, objectMapper.writeValueAsString(message));
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
  }
}
