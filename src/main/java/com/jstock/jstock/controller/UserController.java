package com.jstock.jstock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jstock.jstock.entity.User;
import com.jstock.jstock.entity.UserAccount;
import com.jstock.jstock.mapper.user.UserMapperWithBalance;
import com.jstock.jstock.service.UserAccountService;
import com.jstock.jstock.service.UserService;
import com.jstock.jstock.util.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
  private final UserService userService;
  private final UserAccountService userAccountService;

  @PostMapping("/create")
  public ResponseEntity<Response<User>> createUser(@RequestBody User user) {
    return userService.createUser(user);
  }

  @GetMapping("/balance")
  public ResponseEntity<Response<UserMapperWithBalance>> findUserWithBalanceByEmail(@RequestParam String email) {
    return userService.findUserWithBalanceByEmail(email);
  }

  @GetMapping("/balance/update")
  public ResponseEntity<Response<UserAccount>> updateUserAccountBalance(@RequestParam Long userId,
      @RequestParam Double balance) {
    return userAccountService.updateUserAccountBalance(userId, balance);
  }

  @GetMapping("/update")
  public boolean removeAll() {
    return userAccountService.update();
  }

}
