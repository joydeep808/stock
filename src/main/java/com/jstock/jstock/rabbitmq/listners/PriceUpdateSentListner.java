package com.jstock.jstock.rabbitmq.listners;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jstock.jstock.constants.Constant;
import com.jstock.jstock.dto.order.OrderBook;
import com.jstock.jstock.service.OrderBookService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateSentListner {

  private final ObjectMapper objectMapper;
  private final OrderBookService orderBookService;

  private final List<SseEmitter> userSessionsForTata = new CopyOnWriteArrayList<>();
  private final List<SseEmitter> userSessionsForAirtel = new CopyOnWriteArrayList<>();
  private final List<SseEmitter> userSessionsForJio = new CopyOnWriteArrayList<>();

  @RabbitListener(queues = Constant.UPDATE_SENT_QUEUE)
  public void onMessage(String message) {
    try {
      String symbol = objectMapper.readValue(message, String.class);
      if (symbol.equals("TATA")) {
        userSessionsForTata.add(new SseEmitter());
      } else if (symbol.equals("AIRTEL")) {
        userSessionsForAirtel.add(new SseEmitter());
      } else if (symbol.equals("JIO")) {
        userSessionsForJio.add(new SseEmitter());
      }
      return;
    } catch (Exception e) {
      // TODO: handle exception
    } // System.out.println("PriceUpdateSentListner: " + message);

  }

  public String updateNewPriceToUser(String symbol) {
    OrderBook orderBook = orderBookService.getOrderBook(symbol);
    SseEventBuilder data = SseEmitter.event().name(symbol).data(orderBook.toString());
    if (symbol.equals("TATA")) {
      for (SseEmitter sseEmitter : userSessionsForTata) {
        try {
          sseEmitter.send(data);
        } catch (Exception e) {
          log.error(e.getLocalizedMessage());
        }
      }

    } else if (symbol.equals("AIRTEL")) {
      try {
        for (SseEmitter sseEmitter : userSessionsForAirtel) {
          sseEmitter.send(data);
        }
      } catch (Exception e) {
        log.error(e.getLocalizedMessage());
        // TODO: handle exception
      }
    } else if (symbol.equals("JIO")) {
      try {
        for (SseEmitter sseEmitter : userSessionsForJio) {
          sseEmitter.send(data);
        }
      } catch (Exception e) {
        log.error(e.getLocalizedMessage());
        // TODO: handle exception
      }
    }
    return null;

  }
}
