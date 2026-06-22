package com.fairshare.distributed_expense_splitter.helper;

import com.fairshare.distributed_expense_splitter.entity.Group;
import com.fairshare.distributed_expense_splitter.exception.ExpenseValidationException;
import java.util.ArrayList;
import java.util.List;
import org.openapitools.model.CreateExpenseRequest;
import org.openapitools.model.ExpenseSplitDTO;
import org.openapitools.model.SplitType;
import org.springframework.stereotype.Component;

@Component
public class ExpenseValidator {

  public void validate(CreateExpenseRequest request, Group group) {
    List<String> errors = new ArrayList<>();

    // 1. Math Validations (Context-Free)
    validateTotalAmount(request.getAmount(), errors);
    validateSplitsAndMath(request, errors);

    // // 2. Relationship/Membership Validations (Context-Dependent)
    if (errors.isEmpty()) { // Only do DB/Group checks if the math is valid
      validateGroupMembership(request, group, errors);
    }

    // If any errors were collected, throw them all at once
    if (!errors.isEmpty()) {
      throw new ExpenseValidationException(errors);
    }
  }

  private void validateTotalAmount(Double totalAmount, List<String> errors) {
    if (totalAmount == null || totalAmount <= 0) {
      errors.add("Total amount must be greater than zero.");
    }
  }

  private void validateSplitsAndMath(
      CreateExpenseRequest request,
      List<String> errors) {
    List<ExpenseSplitDTO> splits = request.getSplits();
    if (splits == null || splits.isEmpty()) {
      errors.add("Splits cannot be empty.");
      return;
    }

    // Check for duplicate user IDs in the splits
    long uniqueUsers = splits
        .stream()
        .map(ExpenseSplitDTO::getUserId)
        .distinct()
        .count();
    if (uniqueUsers != splits.size()) {
      errors.add("Duplicate user IDs found in splits.");
    }

    // Handle Math Conversions & Combined Amount Validations

    if (request.getSplitType() != SplitType.EQUAL) {
      Double sumOfSplits = 0.0;
      Double totalPercentage = 0.0;
      // Integer total_shares = 0;

      for (ExpenseSplitDTO split : splits) {
        // If using percentages, convert/verify here
        if (request.getSplitType() == SplitType.EXACT) {
          Double tempAmount = split.getAmount();
          if (tempAmount == null || tempAmount < 0) {
            errors.add("Split amount provided is invalid for user: " + split.getUserId());
            continue;
          }
          sumOfSplits += tempAmount;
        } else if (request.getSplitType() == SplitType.PERCENTAGE) {
          Double tempPercentages = split.getPercentage();
          if (tempPercentages == null || tempPercentages < 0) {
            errors.add("Split percentages provided is invalid for user: " + split.getUserId());
            continue;
          }
          totalPercentage += tempPercentages;
        } else if (request.getSplitType() == SplitType.SHARE) {
          Integer tempShares = split.getShare();
          if (tempShares == null || tempShares < 0) {
            errors.add("Split shares provided is invalid for user: " + split.getUserId());
          }
        }
      }

      // Combined Amount Validation (Sum of splits must equal total amount)
      if (request.getSplitType() == SplitType.PERCENTAGE &&
          Math.abs(totalPercentage - 100.0) > 0.01) {
        errors.add("Total percentage must equal 100%. Current: " + totalPercentage + "%");
      }

      if (request.getSplitType() == SplitType.EXACT &&
          sumOfSplits.compareTo(request.getAmount()) != 0) {
        errors.add("Total amount should be equal to " + request.getAmount() + ", current sum: " + sumOfSplits);
      }
    }
  }

  private void validateGroupMembership(
      CreateExpenseRequest request,
      Group group,
      List<String> errors) {
    // Check if user who is paying is part of group
    if (!group.hasMember(request.getPaidBy())) {
      errors.add("The payer (User ID: " + request.getPaidBy() + ") is not a member of this group.");
    }
    // should I Check if all split userid are part of current group
    if (group.getTotalMemberCount() < request.getSplits().size()) {
      errors.add("More number of splits compared to number of members in group");
    }
    // Validate all split users
    for (ExpenseSplitDTO split : request.getSplits()) {
      if (!group.hasMember(split.getUserId())) {
        errors.add(
            "Split user (User ID: " + split.getUserId() + ") is not a member of this group.");
      }
    }
  }
}
