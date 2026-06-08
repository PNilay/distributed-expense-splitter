package com.fairshare.distributed_expense_splitter.exception;

import com.fairshare.distributed_expense_splitter.entity.ErrorCode;


import lombok.Data;

@Data
public class UserException extends RuntimeException {
    private final ErrorCode errorCode;

  public UserException(String message, ErrorCode code) {
    super(message);
    this.errorCode = code;
  }
}
