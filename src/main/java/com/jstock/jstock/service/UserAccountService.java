package com.jstock.jstock.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.jstock.jstock.constants.Constant;
import com.jstock.jstock.dto.user.UserBalanceDto;
import com.jstock.jstock.entity.UserAccount;
import com.jstock.jstock.repository.UserAccountRepo;
import com.jstock.jstock.service.redis.RedisService;
import com.jstock.jstock.util.Response;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccountService {
  private final RedisService redisService;
  private final UserAccountRepo userAccountRepo;

  public void createUserAccount(UserAccount userAccount) {

    userAccount.setBalance(50000.00);
    userAccountRepo.save(userAccount);
    Map<String, Double> of = Map.of(Constant.INR, userAccount.getBalance());
    UserBalanceDto userBalanceDto = new UserBalanceDto(userAccount.getUserId(), of);
    redisService.set(userAccount.getUserId().toString(), userBalanceDto);

  }

  public ResponseEntity<Response<UserAccount>> updateUserAccountBalance(Long userId, Double balance) {
    UserAccount userAccount = userAccountRepo.findByUserId(userId).orElse(null);
    if (userAccount == null) {
      return Response.sendErrorResponse("User account not found", 404);
    }

    userAccount.setBalance(balance);
    userAccountRepo.save(userAccount);
    UserBalanceDto userBalanceDto = redisService.get(userAccount.getUserId().toString(), UserBalanceDto.class);
    userBalanceDto.getBalances().put(Constant.INR, userAccount.getBalance());
    redisService.update(userAccount.getUserId().toString(), userBalanceDto);
    return Response.sendSuccessResponse("User account updated successfully", userAccount);
  }

  public boolean update() {
    List<UserAccount> all = userAccountRepo.findAll();
    ;
    for (UserAccount userAccount : all) {
      UserBalanceDto userBalanceDto = redisService.get(userAccount.getUserId().toString(), UserBalanceDto.class);
      userBalanceDto.getBalances().put(Constant.INR, userAccount.getBalance());
      redisService.update(userAccount.getUserId().toString(), userBalanceDto);
    }
    return true;
  }

}
