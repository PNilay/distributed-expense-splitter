package com.fairshare.distributed_expense_splitter.controller;

import com.fairshare.distributed_expense_splitter.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openapitools.api.ExpensesApi;
import org.openapitools.model.CreateExpenseRequest;
import org.openapitools.model.ExpenseDTO;
import org.openapitools.model.SettlementRequest;
import org.openapitools.model.ExpensePageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api")
public class ExpenseController implements ExpensesApi {

  private final ExpenseService expenseService;

  public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  private static final Logger LOGGER = LogManager.getLogger(
      ExpenseController.class);

  @Override
  @PostMapping("/expenses")
  public ResponseEntity<ExpenseDTO> createExpense(
      @Valid @RequestBody CreateExpenseRequest createExpenseRequest) {
    LOGGER.info(
        "Expense creation request received for group_id = {}",
        createExpenseRequest.getGroupId());

    ExpenseDTO res = expenseService.createExpense(createExpenseRequest);
    return new ResponseEntity<>(res, HttpStatus.CREATED);
  }

  @GetMapping("/expenses")
  @Override
  public ResponseEntity<List<ExpenseDTO>> getExpenses() {
    LOGGER.warn("Get all Expenses! (NOT REQUIRED)");
    List<ExpenseDTO> list = expenseService.getExpenses();
    return ResponseEntity.ok(list);
  }

  @GetMapping("/expenses/{expenseId}")
  @Override
  public ResponseEntity<ExpenseDTO> getExpense(
      @PathVariable("expenseId") Long expenseId) {
    ExpenseDTO dto = expenseService.getExpense(expenseId);
    return ResponseEntity.ok(dto);
  }

  @PutMapping("/expenses/{expenseId}")
  @Override
  public ResponseEntity<ExpenseDTO> updateExpense(
      @PathVariable("expenseId") Long expenseId,
      @Valid @RequestBody CreateExpenseRequest req) {
    ExpenseDTO res = expenseService.updateExpense(expenseId, req);
    return ResponseEntity.ok(res);
  }

  @DeleteMapping("/expenses/{expenseId}")
  @Override
  public ResponseEntity<Void> deleteExpense(
      @PathVariable("expenseId") Long expenseId) {
    expenseService.deleteExpense(expenseId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/expenses/settle")
  public ResponseEntity<ExpenseDTO> settleDebt(@Valid @RequestBody SettlementRequest settlementRequest) {
    ExpenseDTO res = expenseService.settleDebt(settlementRequest);
    return new ResponseEntity<>(res, HttpStatus.CREATED);
  }

  @GetMapping("/expenses/user/{userId}")
  @Override
  public ResponseEntity<ExpensePageDTO> getUserExpensesPaginated(
      @PathVariable("userId") Long userId,
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
    ExpensePageDTO pageDto = expenseService.getUserExpensesPaginated(userId, page, size);
    return ResponseEntity.ok(pageDto);
  }
}
