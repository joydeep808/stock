package com.jstock.jstock.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.jstock.jstock.constants.Constant;
import com.jstock.jstock.dto.order.CencelOrderDto;
import com.jstock.jstock.dto.order.OrderBook;
import com.jstock.jstock.dto.order.OrderBookBidsDto;
import com.jstock.jstock.entity.Stock;
import com.jstock.jstock.rabbitmq.MessageSender;
import com.jstock.jstock.util.Response;

@Service
// @RequiredArgsConstructor
public class OrderBookService {
  private List<OrderBook> orderBooks = new ArrayList<>();
  private final MessageSender messageSender;
  private final EmiterService emiterService;

  public OrderBookService(StockService stockService, MessageSender messageSender, EmiterService emiterService) {
    this.emiterService = emiterService;
    this.messageSender = messageSender;
    List<Stock> allStocks = stockService.getAllStocks();
    for (Stock stock : allStocks) {
      OrderBook orderBook = new OrderBook(stock.getSymbol(), stock.getPrice());
      orderBooks.add(orderBook);
    }
  }

  public SseEmitter registerUserForGetOrderHistory(String symbol) {
    OrderBook foundOrderBook = orderBooks.stream().filter(o -> o.getSymbol().equals(symbol)).findFirst().orElse(null);
    if (foundOrderBook == null) {
      return null;
    }
    // messageSender.send(Constant.UPDATE_SENT_QUEUE, symbol);
    return emiterService.addUserToSse(symbol);

  }

  public OrderBook getOrderBook(String symbol) {
    for (OrderBook orderBook : orderBooks) {
      if (orderBook.getSymbol().equals(symbol)) {
        return orderBook;
      }
    }
    return null;
  }

  public CencelOrderDto cencelOrder(String id, String symbol) {
    CencelOrderDto cencelOrderDto = new CencelOrderDto();
    if (id != null && symbol != null) {

      OrderBook orderBook = getOrderBook(symbol);
      if (orderBook == null) {
        cencelOrderDto.setSuccess(false);
        return cencelOrderDto;
      }

      if (symbol.equals("ASKS")) {
        LinkedList<OrderBookBidsDto> asks = orderBook.getAsks();
        int size = asks.size();
        int index = 0;
        while (size > index) {
          OrderBookBidsDto orderBookBidsDto = asks.get(index);
          if (orderBookBidsDto.getId() == id) {
            asks.remove(index);
            cencelOrderDto.setSuccess(true);
            cencelOrderDto.setQuantity(orderBookBidsDto.getQuantity());
            cencelOrderDto.setOrderId(orderBookBidsDto.getId());
            cencelOrderDto.setSide("ASKS");
            break;
          }

        }
        return cencelOrderDto;
      } else {
        List<OrderBookBidsDto> bids = orderBook.getBids();
        int size = bids.size();
        int index = 0;
        while (size > index) {
          OrderBookBidsDto orderBookBidsDto = bids.get(index);
          if (orderBookBidsDto.getId() == id) {
            bids.remove(index);
            cencelOrderDto.setSuccess(true);
            cencelOrderDto.setQuantity(orderBookBidsDto.getQuantity());
            cencelOrderDto.setOrderId(orderBookBidsDto.getId());
            cencelOrderDto.setSide("BIDS");
            cencelOrderDto.setPrice(orderBookBidsDto.getPrice());
            break;
          }

        }
      }
      return cencelOrderDto;

    }
    return cencelOrderDto;

  }

}
