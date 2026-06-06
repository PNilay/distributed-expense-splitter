package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.ExpenseService;
import org.openapitools.api.ExpensesApi;
import org.openapitools.model.CreateExpenseRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api")
public class ExpenseController implements ExpensesApi {

  private final ExpenseService expenseService;

  public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  @Override
  @PostMapping("/expenses")
  public ResponseEntity<Void> createExpense(@Valid @RequestBody CreateExpenseRequest createExpenseRequest) {
    try {
      expenseService.createExpense(createExpenseRequest);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception e) {
      return ResponseEntity.status(404).build();
    }
  }
}
