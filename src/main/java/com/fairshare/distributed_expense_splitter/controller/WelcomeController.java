package com.fairshare.distributed_expense_splitter.controller;

import org.openapitools.api.WelcomeApi;
import org.openapitools.model.WelcomeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController implements WelcomeApi {

  @GetMapping("/hello")
  public String sayHello() {
    return "Hello, World!";
  }

  @Override
  public ResponseEntity<WelcomeResponse> getWelcomeMessage() {
    WelcomeResponse response = new WelcomeResponse();
    response.setMessage("Welcome to Expense Splitter API");
    return ResponseEntity.ok(response);
  }
}
