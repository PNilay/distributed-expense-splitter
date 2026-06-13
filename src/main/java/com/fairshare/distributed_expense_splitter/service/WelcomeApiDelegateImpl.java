package com.fairshare.distributed_expense_splitter.service;

import org.openapitools.api.WelcomeApiDelegate;
import org.openapitools.model.WelcomeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WelcomeApiDelegateImpl implements WelcomeApiDelegate {

  @Override
  public ResponseEntity<WelcomeResponse> getWelcomeMessage() {
    WelcomeResponse res = new WelcomeResponse();
    res.setMessage("Welcome to Expense Splitter API");
    return ResponseEntity.ok(res);
  }
}