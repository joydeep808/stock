package com.jstock.jstock.rabbitmq.listners;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jstock.jstock.constants.Constant;
import com.jstock.jstock.dto.order.OrderBook;
import com.jstock.jstock.service.EmiterService;
import com.jstock.jstock.service.OrderBookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateSentListner {

  private final ObjectMapper objectMapper;
  private final EmiterService emiterService;

  public String updateNewPriceToUser(String symbol) {
    try {
      String foundSymbol = objectMapper.readValue(symbol, String.class);
      emiterService.notifyPriceUpdate(foundSymbol);
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
    }
    ;
    return null;
  }
}
