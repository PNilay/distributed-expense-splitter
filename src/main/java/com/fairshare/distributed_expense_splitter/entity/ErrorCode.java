package com.fairshare.distributed_expense_splitter.entity;
public enum ErrorCode {

    USER_NOT_FOUND,
    GROUP_NOT_FOUND,
    EXPENSE_NOT_FOUND,

    USER_ALREADY_EXISTS,
    GROUP_ALREADY_EXISTS,

    VALIDATION_ERROR,
    INVALID_REQUEST,

    DATABASE_ERROR,
    INTERNAL_SERVER_ERROR, INVALID_INPUT
}