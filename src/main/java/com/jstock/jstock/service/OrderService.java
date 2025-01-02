package com.jstock.jstock.service;

import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jstock.jstock.constants.Constant;
import com.jstock.jstock.constants.Constant.OrderSide;
import com.jstock.jstock.constants.Constant.OrderStatus;
import com.jstock.jstock.constants.Constant.OrderType;
import com.jstock.jstock.dto.order.*;
import com.jstock.jstock.dto.user.UserBalanceDto;
import com.jstock.jstock.entity.Order;
import com.jstock.jstock.entity.Order.*;
import com.jstock.jstock.entity.UserBalanceTransaction.TransactionType;
import com.jstock.jstock.entity.Trade;
import com.jstock.jstock.entity.UserBalanceTransaction;
import com.jstock.jstock.rabbitmq.*;
import com.jstock.jstock.repository.*;
import com.jstock.jstock.service.redis.RedisService;
import com.jstock.jstock.util.*;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

  private final RedisService redisService;
  private final OrderBookService orderBookService;
  private final MessageSender messageSender;
  private final OrderRepo orderRepo;
  private final TradeRepo tradeRepo;

  // @Override
  public ResponseEntity<Response<String>> placeOrder(OrderRequest request) {

    // TODO: i have to handle the price order also
    // Because i am currently handling the quantity trades only
    //

    long startTime = System.currentTimeMillis();

    // Log the start of the order placement process
    // log.info("Starting order placement for symbol: {} and user: {}",
    // request.getSymbol(), request.getUserId());

    // Retrieve the order book for the given symbol
    OrderBook orderBook = orderBookService.getOrderBook(request.getSymbol());
    if (orderBook == null) {
      // If the order book does not exist for the symbol, send an error response
      // log.error("Order book not found for symbol: {}", request.getSymbol());
      return Response.sendErrorResponse("Symbol not found", 400);
    }

    // Get the current price from the order book
    Double currentPrice = orderBook.getPrice();
    if (currentPrice != null) {
      // If the current price is not null, check if the requested price is within
      // 5% of the current market price. If not, send an error response
      double upperLimit = currentPrice * 1.05;
      double lowerLimit = currentPrice * 0.95;
      if (request.getPrice() > upperLimit || request.getPrice() < lowerLimit) {
        return Response.sendErrorResponse("Price must be within 5% of current market price: " + currentPrice, 400);
      }
    }

    // Get the user ID from the request
    Long userId = request.getUserId();

    // Call the isMatchable method to check if the order is matchable with the
    // order book
    ResponseEntity<Response<String>> response = isMatchable(orderBook, request, userId);

    // Log the completion of the order placement process and the execution time
    long executionTime = System.currentTimeMillis() - startTime;
    log.info("Order placement completed in {} ms", executionTime);

    // Return the response
    return response;
  }

  private ResponseEntity<Response<String>> isMatchable(OrderBook orderBook, OrderRequest request, Long userId) {
    // log.debug("Checking if order is matchable for type: {}", request.getType());

    if ("BID".equals(request.getType())) {
      try {
        return handleBids(orderBook, request, userId);
      } catch (Exception e) {
        System.out.println(e.getLocalizedMessage());
        return null;
      }
    } else {
      try {
        return handleAsks(orderBook, request, userId);
      } catch (Exception e) {
        System.err.println(e.getLocalizedMessage());
        return null;
      }
    }
  }

  /**
   * Handle Ask
   */

  @Transactional
  private ResponseEntity<Response<String>> handleAsks(OrderBook orderBook, OrderRequest request, Long userId) {
    long startTime = System.currentTimeMillis();
    // log.info("Processing ASK order for user: {} symbol: {} quantity: {} price:
    // {}",
    // userId, request.getSymbol(), request.getQuantity(), request.getPrice());

    Double quantity = request.getQuantity();
    // Retrieve the user balance from redis
    UserBalanceDto userAskBalance = redisService.get(userId.toString(), UserBalanceDto.class);

    // If the user balance does not exist, return an error response
    if (userAskBalance == null) {
      // log.error("User balance not found for user: {}", userId);
      return Response.sendErrorResponse("User balance not found", 400);
    }

    // Get the current stock quantity of the user for the given symbol
    Double askerStockQuantity = userAskBalance.getBalances().getOrDefault(request.getSymbol(), 0.0);
    // Log the current balance
    // log.debug("User {} current stock quantity for symbol {}: {}", userId,
    // request.getSymbol(), askerStockQuantity);

    // Initialize the remaining quantity
    Double remainingQuantity = quantity;
    // Retrieve the list of bids from the order book
    List<OrderBookBidsDto> bids = orderBook.getBids();
    // Initialize the result string
    String result = "Order placed successfully";

    // Create a new order
    Order order = new Order(userId, request.getSymbol(), request.getQuantity(), request.getPrice(),
        OrderSide.SELL, OrderType.MARKET, OrderStatus.PENDING);

    // Log the creation of a new order
    // log.debug("Created new order: {}", order);

    // Loop until the remaining quantity is 0
    int index = 0;
    while (remainingQuantity > 0) {
      // If the bids list is empty, break the loop
      if (bids.isEmpty()) {
        // If the user does not have enough stock balance, return an error response
        if (askerStockQuantity < request.getQuantity()) {
          // log.error("Insufficient stock balance for user: {}", userId);
          return Response.sendErrorResponse("Insufficient stock balance", 400);
        }

        // Update the user balance in redis
        userAskBalance.getBalances().put(request.getSymbol(), askerStockQuantity - request.getQuantity());
        redisService.update(userId.toString(), userAskBalance);

        // Log the addition of an ask order to the order book
        // log.debug("Adding ask order to order book: symbol={}, quantity={}, price={}",
        // request.getSymbol(), remainingQuantity, request.getPrice());

        // Add the ask order to the order book
        synchronized (orderBook) {
          orderBook.getAsks().add(new OrderBookBidsDto(
              request.getSymbol(),
              userId,
              Double.valueOf(remainingQuantity),
              request.getPrice()));

        }
        // Send the order to the message queue
        try {
          messageSender.send(RabbitMqConfig.ORDER_QUEUE, order);
          // log.info("Order sent to message queue successfully");
        } catch (Exception e) {
          // log.error("Failed to send order to message queue", e);
          return Response.sendErrorResponse("Failed to update order status", 500);
        }
        break;
      }

      // Retrieve the first bid from the bids list
      OrderBookBidsDto currentBidsDto = bids.get(index++);

      // Own Order place validation
      if (currentBidsDto.getUserId().equals(userId)) {
        // If the user is trying to place an order against their own order, skip the
        // order
        continue;
      }

      // If the current bid price is lower than the ask price, break the loop
      if (currentBidsDto.getPrice() < request.getPrice()) {
        // log.debug("Current bid price {} is lower than ask price {}",
        // currentBidsDto.getPrice(), request.getPrice());

        // Add the ask order to the order book
        synchronized (orderBook) {
          orderBook.getAsks().add(new OrderBookBidsDto(
              request.getSymbol(),
              userId,
              Double.valueOf(remainingQuantity),
              request.getPrice()));
        }
        // Send the order to the message queue

        break;
      }

      // Calculate the match quantity
      double matchQuantity = Math.min(currentBidsDto.getQuantity(), remainingQuantity);
      // Log the match quantity
      // log.info("Matching quantity: {} at price: {}", matchQuantity,
      // currentBidsDto.getPrice());

      // Swap the balances
      String swapResult = swapBalance(userId, currentBidsDto.getUserId(), matchQuantity, currentBidsDto.getPrice(),
          request.getSymbol(), Constant.ASKS);

      // If the balance swap failed, return an error response
      if (!"SUCCESS".equals(swapResult)) {
        // log.error("Balance swap failed: {}", swapResult);
        return Response.sendErrorResponse("Balance swaped failed", 400);
      }
      // it will update the price of the order book
      orderBook.setPrice(currentBidsDto.getPrice());

      // Create a trade
      Trade trade = new Trade(order.getId(), matchQuantity, userId, currentBidsDto.getPrice(),
          currentBidsDto.getPrice() * matchQuantity);
      trade.setExecutedAt(DateTimeUtil.getCurrentTimeMilis());

      // Send the trade to the message queue
      try {
        messageSender.send(RabbitMqConfig.TRADE_QUEUE, trade);
        // log.info("Trade sent to message queue successfully");
      } catch (Exception e) {
        // log.error("Failed to send trade to message queue", e);
      }

      // Update the remaining quantity
      remainingQuantity -= matchQuantity;
      // Update the bid quantity
      currentBidsDto.setQuantity(currentBidsDto.getQuantity() - matchQuantity);

      // If the remaining quantity is 0, set the order status to FILLED
      if (remainingQuantity == 0) {
        order.setStatus(OrderStatus.FILLED);
        order.setFilledQuantity(quantity);
        try {
          // Send a full success notification to the message queue
          messageSender.send(RabbitMqConfig.FULL_TRADE_SUCCESS_QUEUE, order);
          // log.info("Full success notification sent for order: {}", order.getId());
        } catch (Exception e) {
          // log.error("Failed to send full success notification", e);
        }
      } else {
        // If the remaining quantity is not 0, set the order status to PARTIALLY_FILLED
        order.setStatus(OrderStatus.PARTIALLY_FILLED);
        order.setFilledQuantity(quantity - remainingQuantity);
        try {
          // Send a partial success notification to the message queue
          messageSender.send(RabbitMqConfig.PARTIAL_TRADE_QUEUE, order);
          // log.info("Partial success notification sent for order: {}", order.getId());
        } catch (Exception e) {
          // log.error("Failed to send partial success notification", e);
        }
      }

      // If the bid quantity is 0, remove the bid from the order book
      if (currentBidsDto.getQuantity() == 0) {
        orderBook.getBids().remove(index);
        // log.debug("Removed completed bid from order book");
      }

      // If the remaining quantity is greater than 0 and the bids list is empty, add
      // the remaining
      // quantity to the order book as an ask order
      if (remainingQuantity > 0 && (bids.isEmpty() || bids.get(index) == null)) {
        orderBook.getAsks().add(new OrderBookBidsDto(
            request.getSymbol(),
            userId,
            Double.valueOf(remainingQuantity),
            request.getPrice()));
        // log.debug("Added remaining ask order to order book");
        break;
      }
    }

    // Log the completion of the ASK order processing
    long executionTime = System.currentTimeMillis() - startTime;
    // log.info("ASK order processing completed in {} ms", executionTime);
    // Return the result
    return Response.sendSuccessResponse(result);
  }

  @Transactional
  private ResponseEntity<Response<String>> handleBids(OrderBook orderBook, OrderRequest request, Long userId) {
    // Get the current timestamp to track the execution time
    long startTime = System.currentTimeMillis();
    // log.info("Processing BID order for user: {} symbol: {} quantity: {} price:
    // {}",
    // userId, request.getSymbol(), request.getQuantity(), request.getPrice());

    // Get the list of asks from the order book
    List<OrderBookBidsDto> asks = orderBook.getAsks();

    // Initialize the remaining quantity to the requested quantity
    Double remainingQuantity = request.getQuantity();

    // Get the user balance from Redis
    UserBalanceDto userBalance = redisService.get(userId.toString(), UserBalanceDto.class);
    if (userBalance == null) {
      // If the user balance is not found, send an error response
      // log.error("User balance not found for user: {}", userId);
      return Response.sendErrorResponse("User balance not found", 400);
    }

    // Get the user's current INR balance from the user balance
    Double userInrBalance = userBalance.getBalances().getOrDefault(Constant.INR, 0.0);

    // Calculate the required balance for the order
    Double requiredBalance = request.getQuantity() * request.getPrice();

    // log.debug("User {} current INR balance: {}, required balance: {}",
    // userId, userInrBalance, requiredBalance);

    // If the user's INR balance is less than the required balance, send an error
    // response
    if (userInrBalance < requiredBalance) {
      // log.warn("Insufficient INR balance for user: {}. Required: {}, Available:
      // {}",
      // userId, requiredBalance, userInrBalance);
      return Response.sendErrorResponse("Insufficient INR balance", 400);
    }

    // Create a new order with the requested parameters
    Order order = new Order(userId, request.getSymbol(), request.getQuantity(), request.getPrice(),
        OrderSide.BUY, OrderType.valueOf("MARKET"), OrderStatus.PENDING);

    // log.debug("Created new order: {}", order);

    // Send the order to the message queue
    try {
      messageSender.send(RabbitMqConfig.ORDER_QUEUE, order);
      // log.info("Order sent to message queue successfully");
    } catch (Exception e) {
      // If there is an error sending the order to the message queue, log the error
      // and send an error response
      // log.error("Failed to send order to message queue", e);
      return Response.sendErrorResponse("Failed to place order", 500);
    }

    // While there is still a remaining quantity
    int index = 0;
    while (remainingQuantity > 0) {
      // If there are no asks left in the order book, add the remaining quantity as a
      // bid to the order book
      if (asks.isEmpty()) {
        synchronized (orderBook) {
          orderBook.getBids().add(
              new OrderBookBidsDto(request.getSymbol(), userId, Double.valueOf(remainingQuantity), request.getPrice()));
        }
        // log.info("Added remaining bid to order book: quantity={}, price={}",
        // remainingQuantity, request.getPrice());
        // Update the user balance to reflect the bid
        Double userBids = userBalance.getBalances().getOrDefault(request.getSymbol(), 0.0);

        userBalance.getBalances().put(Constant.INR, userInrBalance - requiredBalance);
        userBalance.getBalances().put(request.getSymbol(), userBids + remainingQuantity);
        redisService.update(userId.toString(), userBalance);
        // TODO: i have to sent the request to my orderBook entity
        return Response.sendSuccessResponse("Bid order placed", null);
      }

      // Get the current ask from the order book
      OrderBookBidsDto currentAsk = asks.get(index++);

      // If the current ask is the same user as the order, skip it
      if (currentAsk.getUserId().equals(userId)) {
        continue;
      }

      // If the current ask price is higher than the bid price, add the remaining
      // quantity as a bid to the order book
      if (currentAsk.getPrice() > request.getPrice()) {
        synchronized (orderBook) {
          orderBook.getBids().add(
              new OrderBookBidsDto(request.getSymbol(), userId, Double.valueOf(remainingQuantity), request.getPrice()));
        }
        // TODO: i have to sent the request to the orderbook entity
        // log.debug("Current ask price {} is higher than bid price {}",
        // currentAsk.getPrice(), request.getPrice());
        break;
      }

      // The match is happens

      // Calculate the quantity to match with the current ask
      double matchQuantity = Math.min(currentAsk.getQuantity(), remainingQuantity);
      // log.info("Matching quantity: {} at price: {}", matchQuantity,
      // currentAsk.getPrice());

      // Call the swapBalance method to swap the balance of the current ask user and
      // the order user
      String swapResult = swapBalance(currentAsk.getUserId(), userId, matchQuantity, currentAsk.getPrice(),
          request.getSymbol(), Constant.BID);

      // If the swap balance failed, log the error and return an error response
      if (!"SUCCESS".equals(swapResult)) {
        if (remainingQuantity < request.getQuantity()) {
          order.setStatus(OrderStatus.PARTIALLY_FILLED);
          order.setFilledQuantity(request.getQuantity() - remainingQuantity);
          try {
            messageSender.send(RabbitMqConfig.PARTIAL_TRADE_QUEUE, order);
            // log.info("Partial success notification sent for order: {}", order.getId());
          } catch (Exception e) {
            // log.error("Failed to send partial success notification", e);
          }
        }
        // log.error("Balance swap failed: {}", swapResult);
        return Response.sendErrorResponse(swapResult, 400);
      }

      // TODO : i have to sent the price history to my entity

      // If the swap balance succeeded, update the order status
      orderBook.setPrice(currentAsk.getPrice());
      // Create a new trade with the matched quantity and price
      Trade trade = new Trade(order.getId().toString(), matchQuantity, currentAsk.getUserId(), currentAsk.getPrice(),
          currentAsk.getPrice() * matchQuantity);
      trade.setExecutedAt(DateTimeUtil.getCurrentTimeMilis());

      // Send the trade to the message queue
      try {
        messageSender.send(RabbitMqConfig.TRADE_QUEUE, trade);
        // log.info("Trade sent to message queue successfully");
      } catch (Exception e) {
        // log.error("Failed to send trade to message queue", e);
      }

      // Update the remaining quantity and the current ask quantity
      remainingQuantity -= matchQuantity;
      currentAsk.setQuantity(currentAsk.getQuantity() - matchQuantity);

      // If the current ask quantity is 0, remove it from the order book
      if (currentAsk.getQuantity() == 0) {
        asks.remove(index);
        // log.debug("Removed completed ask from order book");
      }
    }

    long executionTime = System.currentTimeMillis() - startTime;
    log.info("BID order processing completed in {} ms", executionTime);
    return Response.sendSuccessResponse("Order processed successfully");
  }

  @Transactional
  private String swapBalance(Long asker, Long bidder, Double quantity, Double price, String symbol, String type) {
    long startTime = System.currentTimeMillis(); // Record the start time for performance measurement
    // log.info("Starting balance swap - Asker: {}, Bidder: {}, Quantity: {}, Price:
    // {}, Symbol: {}",
    // asker, bidder, quantity, price, symbol); // Log the beginning of the balance
    // swap process

    if (asker == null || bidder == null) { // Check if the user IDs are valid
      // log.error("Invalid user IDs - Asker: {}, Bidder: {}", asker, bidder); // Log
      // an error if user IDs are invalid
      return "Invalid user IDs"; // Return error message for invalid user IDs
    }

    if (symbol == null || symbol.isEmpty()) { // Check if the provided symbol is valid
      // log.error("Invalid symbol provided"); // Log an error if symbol is invalid
      return "Invalid symbol"; // Return error message for invalid symbol
    }

    UserBalanceDto askerBalance = redisService.get(asker.toString(), UserBalanceDto.class); // Retrieve asker's balance
                                                                                            // from Redis
    UserBalanceDto bidderBalance = redisService.get(bidder.toString(), UserBalanceDto.class); // Retrieve bidder's
                                                                                              // balance from Redis

    if (askerBalance == null || bidderBalance == null) { // Check if balances are successfully retrieved
      // log.error("Invalid user balances - Asker balance exists: {}, Bidder balance
      // exists: {}",
      // askerBalance != null, bidderBalance != null); // Log an error if balances are
      // invalid
      return "Invalid user balances"; // Return error message for invalid balances
    }

    Double totalPrice = quantity * price; // Calculate total transaction price
    // log.debug("Total transaction price: {}", totalPrice); // Log the total
    // transaction price

    Double bidderInrBalance = bidderBalance.getBalances().getOrDefault(Constant.INR, 0.0); // Get INR balance of bidder
    if (bidderInrBalance < totalPrice) { // Check if bidder has sufficient INR balance
      // log.warn("Insufficient INR balance for bidder. Required: {}, Available: {}",
      // totalPrice, bidderInrBalance); // Log a warning if insufficient balance
      return "Insufficient INR balance for bidder"; // Return error for insufficient balance
    }

    Double askerStockBalance = askerBalance.getBalances().getOrDefault(symbol, 0.0); // Get stock balance of asker
    if (askerStockBalance < quantity) { // Check if asker has sufficient stock balance
      // log.warn("Insufficient stock balance for asker. Required: {}, Available: {}",
      // quantity, askerStockBalance); // Log a warning if insufficient stock
      return "Insufficient stock balance for asker"; // Return error for insufficient stock
    }

    // Log the initial balances before swap
    // log.debug("Initial balances - Asker: INR={}, {}={}, Bidder: INR={}, {}={}",
    // askerBalance.getBalances().getOrDefault(Constant.INR, 0.0),
    // symbol, askerStockBalance,
    // bidderInrBalance,
    // symbol, bidderBalance.getBalances().getOrDefault(symbol, 0.0));

    // Update asker's balances after swap
    Double askerInr = askerBalance.getBalances().getOrDefault(Constant.INR, 0.0) + totalPrice; // Calculate asker's new
                                                                                               // INR balance
    Map<String, Double> newAskerBalances = new HashMap<>(askerBalance.getBalances()); // Create a copy of asker's
                                                                                      // current balances
    newAskerBalances.put(Constant.INR, askerInr); // Update INR balance in asker's balances
    newAskerBalances.put(symbol, askerStockBalance - quantity); // Update stock balance in asker's balances
    askerBalance.updateBalances(newAskerBalances); // Apply balance updates to asker's balance object

    // Update bidder's balances after swap
    Double bidderStockQuantity = bidderBalance.getBalances().getOrDefault(symbol, 0.0); // Get current stock quantity of
                                                                                        // bidder
    Map<String, Double> newBidderBalances = new HashMap<>(bidderBalance.getBalances()); // Create a copy of bidder's
                                                                                        // current balances
    newBidderBalances.put(Constant.INR, bidderInrBalance - totalPrice); // Update INR balance in bidder's balances
    newBidderBalances.put(symbol, bidderStockQuantity + quantity); // Update stock balance in bidder's balances
    bidderBalance.updateBalances(newBidderBalances); // Apply balance updates to bidder's balance object

    // Log the updated balances after swap
    // log.debug("Updated balances - Asker: INR={}, {}={}, Bidder: INR={}, {}={}",
    // askerInr,
    // symbol, askerStockBalance - quantity,
    // bidderInrBalance - totalPrice,
    // symbol, bidderStockQuantity + quantity);

    try {
      redisService.update(asker.toString(), askerBalance); // Update asker's balance in Redis
      redisService.update(bidder.toString(), bidderBalance); // Update bidder's balance in Redis
      // log.info("Successfully updated balances in Redis"); // Log successful update
      // of balances in Redis
    } catch (Exception e) {
      // log.error("Failed to update balances in Redis", e); // Log error if update
      // fails
      return "Failed to update balances"; // Return error message for failed update
    }

    if (type.equals(Constant.BID)) { // Check if the transaction type is BID
      try {
        messageSender.send(RabbitMqConfig.USER_BALANCE_QUEUE,
            new UserBalanceTransaction(bidder, totalPrice, TransactionType.BID, false, symbol)); // Send balance
                                                                                                 // transaction to queue
        // log.info("Successfully sent balance transaction to queue"); // Log successful
        // transaction sending
      } catch (Exception e) {
        // log.error("Failed to send balance transaction to queue", e); // Log error if
        // transaction sending fails
      }
    }

    long executionTime = System.currentTimeMillis() - startTime; // Calculate execution time
    log.info("Balance swap completed successfully in {} ms", executionTime); //
    // Log successful completion of balance
    // swap
    return "SUCCESS"; // Return success message
  }

  // @Override

  /**
   * This method cancels an order with the given order ID and symbol. It first
   * checks if the order exists and if it has not been cancelled before. If the
   * order exists and has not been cancelled, it reverses the order quantity in
   * the user's balance and sends the order ID to the message queue to notify
   * the order service to cancel the order.
   * 
   * @param orderId The ID of the order to be cancelled
   * @param symbol  The symbol of the stock
   * @return A ResponseEntity containing a Response object with a success or
   *         error message and a status code
   */
  public ResponseEntity<Response<String>> cancelOrder(String orderId, String symbol) {
    Long userId = 1l;
    CencelOrderDto cencelOrder = orderBookService.cencelOrder(orderId, symbol);
    if (!cencelOrder.isSuccess()) {
      // If the order does not exist or has already been cancelled, return an
      // error response with a 400 status code
      return Response.sendErrorResponse("Order not found or already cancelled", 400);
    }
    try {
      // Reverse the order quantity in the user's balance
      reverseOrderQuantity(userId, cencelOrder);
      // Send the order ID to the message queue to notify the order service to
      // cancel the order
      messageSender.send(RabbitMqConfig.ORDER_QUEUE, orderId);
      // Return a success response with a 200 status code
      return Response.sendSuccessResponse("Order cancelled successfully if there");
    } catch (Exception e) {
      // If an exception occurs while cancelling the order, return an error
      // response with a 400 status code
      return Response.sendErrorResponse("Order not found or already cancelled", 400);
    }
  }

  /**
   * Reverses the quantity of the given order in the user's balance.
   * 
   * This method takes a user ID, an order ID, and a symbol as input, and reverses
   * the quantity of the order in the user's balance. It first retrieves the
   * user's
   * balance from Redis, then checks if the order side is an ask or a bid. If the
   * order side is an ask, it adds the quantity of the order to the user's balance
   * for the given symbol. If the order side is a bid, it adds the quantity of the
   * order to the user's balance for the INR symbol. Finally, it updates the
   * user's
   * balance in Redis.
   * 
   * @param userId      The ID of the user whose balance is being reversed.
   * @param cencelOrder The order that is being reversed.
   * @return True if the balance is reversed successfully, false otherwise.
   */
  private boolean reverseOrderQuantity(Long userId, CencelOrderDto cencelOrder) {
    // Retrieve the user's balance from Redis
    UserBalanceDto userBalance = redisService.get(userId.toString(), UserBalanceDto.class);

    // Get the symbol of the order
    String symbol = cencelOrder.getSymbol();

    // Get the quantity of the order
    Double quantity = cencelOrder.getQuantity();

    // Check if the order side is an ask or a bid
    if (cencelOrder.getSide().equals(Constant.ASKS)) {
      // If the order side is an ask, add the quantity to the user's balance for the
      // given symbol
      messageSender.send(RabbitMqConfig.USER_BALANCE_QUEUE,
          new UserBalanceTransaction(userId, quantity, TransactionType.ASKS, true, symbol));
      Double currentBalance = userBalance.getBalances().getOrDefault(symbol, 0.0);
      userBalance.getBalances().put(symbol, currentBalance + quantity);
    } else {
      // If the order side is a bid, add the quantity to the user's balance for the
      // INR symbol
      messageSender.send(RabbitMqConfig.USER_BALANCE_QUEUE,
          new UserBalanceTransaction(userId, quantity, TransactionType.ASKS, true, Constant.INR));
      Double currentBalance = userBalance.getBalances().getOrDefault(Constant.INR, 0.0);
      userBalance.getBalances().put(Constant.INR, currentBalance + quantity);
    }

    // Update the user's balance in Redis
    redisService.update(userId.toString(), userBalance);

    // Return true if the balance is reversed successfully
    return true;
  }

  // @Override
  public ResponseEntity<Response<Page<Order>>> getOrdersForUser(HttpServletRequest request, Integer page, Long userId) {
    // Long userId = (long) request.getAttribute(Constant.USER_ID);
    Pageable pageable = PageRequest.of(page <= 0 ? 0 : page - 1, 10);
    Page<Order> foundOrder = orderRepo.findAllByUserId(userId, pageable);

    if (foundOrder == null || foundOrder.isEmpty()) {
      return Response.sendErrorResponse("No orders found", 404);
    }
    return Response.sendSuccessResponse("Orders found", foundOrder);
  }

  public ResponseEntity<Response<List<Trade>>> getTradesByOrderId(String orderId) {
    List<Trade> foundTrades = tradeRepo.findByOrderId(orderId);
    if (foundTrades == null || foundTrades.isEmpty()) {
      return Response.sendErrorResponse(" trades not found", 404);
    }
    return Response.sendSuccessResponse(" trades found", foundTrades);

  }

  // @Override
  public ResponseEntity<Response<OrderBook>> getOrderBook(String symbol) {
    OrderBook orderBook = orderBookService.getOrderBook(symbol);
    if (orderBook == null) {
      return Response.sendErrorResponse(symbol + " order book not found", 404);
    }
    return Response.sendSuccessResponse(symbol + " order book found", orderBook);
  }

}
