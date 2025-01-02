package com.jstock.jstock.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.jstock.jstock.entity.User;
import com.jstock.jstock.entity.UserAccount;
import com.jstock.jstock.mapper.user.UserMapperWithBalance;
import com.jstock.jstock.repository.UserRepo;
import com.jstock.jstock.util.Response;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepo userRepo;
  private final UserAccountService userAccountService;

  public ResponseEntity<Response<User>> createUser(User user) {
    // userRepo.save(name, email, password);
    User foundUser = userRepo.findUserByEmail(user.getEmail()).orElse(null);
    if (foundUser != null) {
      return Response.sendErrorResponse("User already exists", 400);
    }
    userRepo.save(user);
    userAccountService.createUserAccount(new UserAccount(user.getId()));
    return Response.sendSuccessResponse("User created successfully", user);
  }

  public ResponseEntity<Response<UserMapperWithBalance>> findUserWithBalanceByEmail(String email) {
    UserMapperWithBalance foundUserAccountDetails = userRepo.findUserWithBalanceByEmail(email).orElse(null);
    if (foundUserAccountDetails == null) {
      return Response.sendErrorResponse("User not found", 404);
    }
    return Response.sendSuccessResponse("User found", foundUserAccountDetails);
  }
}
