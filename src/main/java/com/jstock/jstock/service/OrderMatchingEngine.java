// package com.jstock.jstock.service;

// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.util.StopWatch;

// import com.jstock.jstock.constants.Constant;
// import com.jstock.jstock.dto.order.OrderBook;
// import com.jstock.jstock.dto.order.OrderBookBidsDto;
// import com.jstock.jstock.dto.order.OrderRequest;
// import com.jstock.jstock.dto.user.UserBalanceDto;
// import com.jstock.jstock.entity.Order;
// import com.jstock.jstock.entity.Order.OrderSide;
// import com.jstock.jstock.entity.Order.OrderStatus;
// import com.jstock.jstock.entity.Order.OrderType;
// import com.jstock.jstock.rabbitmq.MessageSender;
// import com.jstock.jstock.repository.OrderRepo;
// import com.jstock.jstock.repository.TradeRepo;
// import com.jstock.jstock.service.redis.RedisService;
// import com.jstock.jstock.util.Response;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.log4j.Log4j2;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @RequiredArgsConstructor
// @Service
// public class OrderMatchingEngine {

// private final RedisService redisService;
// private final OrderBookService orderBookService;
// private final MessageSender messageSender;
// private final OrderRepo orderRepo;
// private final TradeRepo tradeRepo;

// public ResponseEntity<Response<Object>> placeOrder(OrderRequest request) {
// StopWatch stopWatch = new StopWatch("PlaceOrder");
// stopWatch.start("InitialOrderProcessing");

// log.info("Received order request for symbol: {}, type: {}, quantity: {},
// price: {}, userId: {}",
// request.getSymbol(), request.getType(), request.getQuantity(),
// request.getPrice(), request.getUserId());

// OrderBook orderBook = orderBookService.getOrderBook(request.getSymbol());
// if (orderBook == null) {
// log.error("Symbol not found: {}", request.getSymbol());
// stopWatch.stop();
// log.info("Order placement failed - Total execution time: {}ms",
// stopWatch.getTotalTimeMillis());
// return Response.sendErrorResponse("Symbol not found", 400);
// }

// Long userId = request.getUserId();
// stopWatch.stop();
// log.debug("Initial order processing completed in {}ms",
// stopWatch.getLastTaskTimeMillis());

// return isMatchable(orderBook, request, userId, stopWatch);
// }

// private ResponseEntity<Response<Object>> isMatchable(OrderBook orderBook,
// OrderRequest request, Long userId,
// StopWatch stopWatch) {
// stopWatch.start("OrderMatching");
// log.debug("Starting order matching process for order type: {}",
// request.getType());

// ResponseEntity<Response<Object>> result;
// if ("BID".equals(request.getType())) {
// result = handleBids(orderBook, request, userId);
// } else {
// result = handleAsks(orderBook, request, userId);
// }

// stopWatch.stop();
// log.info("Order matching completed - Total execution time: {}ms",
// stopWatch.getTotalTimeMillis());
// return result;
// }

// private ResponseEntity<Response<Object>> handleAsks(OrderBook orderBook,
// OrderRequest request, Long userId) {
// StopWatch stopWatch = new StopWatch("HandleAsks");
// stopWatch.start("InitialValidation");

// log.info("Processing ASK order - Symbol: {}, Quantity: {}, Price: {}",
// request.getSymbol(), request.getQuantity(), request.getPrice());

// int quantity = request.getQuantity();
// UserBalanceDto userAskBalance = redisService.get(userId.toString(),
// UserBalanceDto.class);

// if (userAskBalance == null) {
// log.error("User balance not found for userId: {}", userId);
// stopWatch.stop();
// return Response.sendErrorResponse("User balance not found", 400);
// }

// Double askerStockQuantity =
// userAskBalance.getBalances().getOrDefault(request.getSymbol(), 0.0);
// log.debug("User current balance - Stock: {}, Quantity: {}",
// request.getSymbol(), askerStockQuantity);

// if (quantity > (askerStockQuantity * 0.1)) {
// log.warn("Order quantity {} exceeds 10% of available stock {}", quantity,
// askerStockQuantity);
// stopWatch.stop();
// return Response.sendErrorResponse("Requested quantity exceeds 10% of
// available stock", 400);
// }

// stopWatch.stop();
// stopWatch.start("OrderProcessing");

// int remainingQuantity = quantity;
// List<OrderBookBidsDto> bids = orderBook.getBids();
// String result = "Order placed successfully";

// Order order = new Order(userId, request.getSymbol(), request.getQuantity(),
// request.getPrice(),
// OrderSide.SELL, OrderType.valueOf(request.getType()), OrderStatus.PENDING);

// log.info("Created new order with ID: {}", order.getId());

// while (remainingQuantity > 0) {
// if (bids.isEmpty()) {
// log.debug("No matching bids found, creating new ask order");
// stopWatch.start("EmptyBidsProcessing");

// if (askerStockQuantity < request.getQuantity()) {
// log.error("Insufficient stock balance. Required: {}, Available: {}",
// request.getQuantity(), askerStockQuantity);
// stopWatch.stop();
// return Response.sendErrorResponse("Insufficient stock balance", 400);
// }

// // Rest of the empty bids processing...
// stopWatch.stop();
// log.debug("Empty bids processing completed in {}ms",
// stopWatch.getLastTaskTimeMillis());
// }

// // Continue with existing logic, adding logs at key points...
// OrderBookBidsDto currentBidsDto = bids.get(0);
// log.debug("Processing bid - Price: {}, Quantity: {}",
// currentBidsDto.getPrice(), currentBidsDto.getQuantity());

// // Add timing for each matched trade
// if (currentBidsDto.getPrice() >= request.getPrice()) {
// stopWatch.start("TradeProcessing");
// double matchQuantity = Math.min(currentBidsDto.getQuantity(),
// remainingQuantity);
// log.info("Match found - Quantity: {}, Price: {}", matchQuantity,
// currentBidsDto.getPrice());

// String swapResult = swapBalance(userId, currentBidsDto.getUserId(),
// matchQuantity,
// currentBidsDto.getPrice(), request.getSymbol(), Constant.ASKS);

// stopWatch.stop();
// log.debug("Trade processing completed in {}ms",
// stopWatch.getLastTaskTimeMillis());

// if (!"SUCCESS".equals(swapResult)) {
// log.error("Balance swap failed: {}", swapResult);
// return Response.sendErrorResponse(swapResult, 400);
// }

// // Continue with existing trade processing...
// }
// }

// stopWatch.stop();
// log.info("Ask order processing completed - Total execution time: {}ms",
// stopWatch.getTotalTimeMillis());
// return Response.sendSuccessResponse(result, null);
// }

// private String swapBalance(Long asker, Long bidder, Double quantity, Double
// price, String symbol, String type) {
// StopWatch stopWatch = new StopWatch("SwapBalance");
// stopWatch.start("Validation");

// log.info("Starting balance swap - Asker: {}, Bidder: {}, Quantity: {}, Price:
// {}, Symbol: {}",
// asker, bidder, quantity, price, symbol);

// if (asker == null || bidder == null) {
// log.error("Invalid user IDs - Asker: {}, Bidder: {}", asker, bidder);
// return "Invalid user IDs";
// }

// stopWatch.stop();
// stopWatch.start("BalanceRetrieval");

// UserBalanceDto askerBalance = redisService.get(asker.toString(),
// UserBalanceDto.class);
// UserBalanceDto bidderBalance = redisService.get(bidder.toString(),
// UserBalanceDto.class);

// if (askerBalance == null || bidderBalance == null) {
// log.error("Invalid balance objects - Asker balance: {}, Bidder balance: {}",
// askerBalance != null, bidderBalance != null);
// return "Invalid user balances";
// }

// stopWatch.stop();
// stopWatch.start("BalanceProcessing");

// Double totalPrice = quantity * price;
// log.debug("Calculated total price: {}", totalPrice);

// // Continue with existing balance swap logic...

// stopWatch.stop();
// log.info("Balance swap completed - Total execution time: {}ms",
// stopWatch.getTotalTimeMillis());
// return "SUCCESS";
// }
// }
