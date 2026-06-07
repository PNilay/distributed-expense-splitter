package com.fairshare.distributed_expense_splitter.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldError {

    private String field;
    private Object rejectedValue;
    private String message;
}