package com.jstock.jstock.dto.order;

import java.util.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderBook {

  private String symbol;
  private Double price;
  private LinkedList<OrderBookBidsDto> asks = new LinkedList<>(); // asks should be sorted in assending order
  private List<OrderBookBidsDto> bids = new LinkedList<>(); // bids should be sorted in descending order

  public OrderBook(String symbol, Double price) {
    this.symbol = symbol;
    this.price = price;
  }

  public boolean addAsk(OrderBookBidsDto OrderBookBidsDto) {
    if (OrderBookBidsDto != null && OrderBookBidsDto.getPrice() != null) {
      this.asks.add(OrderBookBidsDto); // This should work if asks is a mutable list
      if (this.asks.size() > 1) {
        this.asks.sort((OrderBookBidsDto o1, OrderBookBidsDto o2) -> {
          if (o1.getPrice() == null)
            return 1; // o1 is considered larger if price is null
          if (o2.getPrice() == null)
            return -1; // o2 is considered larger if price is null
          return o1.getPrice().compareTo(o2.getPrice());
        });
      }
      return true;

    } else {
      System.out.println("Invalid BidsDto: price is null");
      return false;

    }
  }

  public boolean addBid(OrderBookBidsDto OrderBookBidsDto) {
    if (OrderBookBidsDto != null && OrderBookBidsDto.getPrice() != null) {
      this.bids.add(OrderBookBidsDto); // This should work if bids is a mutable list
      if (this.bids.size() > 1) {
        bids.sort((OrderBookBidsDto o1, OrderBookBidsDto o2) -> {
          if (o1.getPrice() == null)
            return -1; // o1 is considered smaller if price is null
          if (o2.getPrice() == null)
            return 1; // o2 is considered smaller if price is null
          return o2.getPrice().compareTo(o1.getPrice());
        });

      }
      return true;
    } else {
      System.out.println("Invalid BidsDto: price is null");
      return false;
    }
  }

  public boolean removeAsk(OrderBookBidsDto OrderBookBidsDto) {
    return this.asks.remove(OrderBookBidsDto);
  }

  public boolean removeBid(OrderBookBidsDto OrderBookBidsDto) {
    return this.bids.remove(OrderBookBidsDto);
  }

  public boolean updatePrice(Double price) {
    synchronized (this) {
      this.price = price;
      return true;
    }
  }

  public OrderBookBidsDto getFirstAsk() {
    return this.asks.getFirst();
  }

  public OrderBookBidsDto getFirstBid() {
    return this.bids.getFirst();
  }

}