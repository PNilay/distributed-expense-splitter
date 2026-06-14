package com.fairshare.distributed_expense_splitter.exception;

import java.util.List;

public class ExpenseValidationException extends RuntimeException {
    private final List<String> errors;

    public ExpenseValidationException(List<String> errors) {
        super("Expense validation failed: " + String.join(", ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() { return errors; }
}
