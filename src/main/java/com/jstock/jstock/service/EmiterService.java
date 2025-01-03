package com.jstock.jstock.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.jstock.jstock.dto.order.OrderBook;
import com.jstock.jstock.entity.Stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmiterService {

  private static final ExecutorService execuatorService = Executors.newFixedThreadPool(100);

  private final OrderBookService orderBookService;

  private final List<SseEmitter> userSessionsForTata = new CopyOnWriteArrayList<>();
  private final List<SseEmitter> userSessionsForAirtel = new CopyOnWriteArrayList<>();
  private final List<SseEmitter> userSessionsForJio = new CopyOnWriteArrayList<>();

  public SseEmitter addEmitter(List<SseEmitter> emitters) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Long.MAX_VALUE to avoid timeout
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter)); // Remove emitter when connection closes
    emitter.onTimeout(() -> emitters.remove(emitter));

    return emitter;
  }

  public SseEmitter addEmiterToLocalVariable(String symbol, List<SseEmitter> userSession) {
    SseEmitter emitter = addEmitter(userSession);
    userSessionsForTata.add(emitter);
    try {
      emitter.send(SseEmitter.event().name("price").data(orderBookService.getOrderBook(symbol).toString()));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return emitter;
  }

  public SseEmitter addUserToSse(String stockSymbol) {
    try {
      if (stockSymbol.equals("TATA")) {
        return addEmiterToLocalVariable(stockSymbol, userSessionsForTata);
      } else if (stockSymbol.equals("AIRTEL")) {
        return addEmiterToLocalVariable(stockSymbol, userSessionsForAirtel);
      } else if (stockSymbol.equals("JIO")) {
        return addEmiterToLocalVariable(stockSymbol, userSessionsForJio);
      }
      return null;
    } catch (Exception e) {
      log.error(e.getLocalizedMessage());
      return null;
    }
  }

  public void notifyPriceUpdate(String stockSymbol) {
    OrderBook orderBook = orderBookService.getOrderBook(stockSymbol);
    SseEventBuilder data = SseEmitter.event().name(stockSymbol).data(orderBook.toString());
    if (stockSymbol.equals("TATA")) {
      for (SseEmitter sseEmitter : userSessionsForTata) {
        try {
          execuatorService.submit(() -> {
            try {
              sseEmitter.send(data);

            } catch (Exception e) {
            }
          });
        } catch (Exception e) {
          log.error(e.getLocalizedMessage());
        }
      }

    } else if (stockSymbol.equals("AIRTEL")) {
      try {
        for (SseEmitter sseEmitter : userSessionsForAirtel) {
          execuatorService.submit(() -> {
            try {
              sseEmitter.send(data);
            } catch (Exception e) {
              // TODO: handle exception
            }
          });
        }
      } catch (Exception e) {
        log.error(e.getLocalizedMessage());
      }
    } else if (stockSymbol.equals("JIO")) {
      try {
        for (SseEmitter sseEmitter : userSessionsForJio) {
          execuatorService.submit(() -> {
            try {
              sseEmitter.send(data);
            } catch (Exception e) {
              // TODO: handle exception
            }
          });
        }
      } catch (Exception e) {
        log.error(e.getLocalizedMessage());
      }
    }
  }
}
