package com.fairshare.distributed_expense_splitter.exception;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;


import lombok.Data;

@Data
public class GroupException extends RuntimeException {
    private final ErrorCode errorCode;

  public GroupException(String message, ErrorCode code) {
    super(message);
    this.errorCode = code;
  }
}
