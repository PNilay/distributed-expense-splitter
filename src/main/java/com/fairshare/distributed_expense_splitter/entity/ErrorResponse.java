package com.fairshare.distributed_expense_splitter.entity;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

  private OffsetDateTime timestamp;
  private Integer status;
  private String error;
  private String code;
  private String message;
  private String path;
  private String traceId;
  private List<FieldError> fieldErrors;
}


// Sample Error Response:
// {
//   "timestamp": "2026-06-07T11:45:32.123Z",
//   "status": 400,
//   "error": "Bad Request",
//   "code": "VALIDATION_ERROR",
//   "message": "Request validation failed",
//   "path": "/api/v1/users",
//   "traceId": "c2f9ab1d8f2a",
//   "fieldErrors": [
//     {
//       "field": "email",
//       "rejectedValue": "abc",
//       "message": "Invalid email address"
//     },
//     {
//       "field": "name",
//       "rejectedValue": "",
//       "message": "Name is required"
//     }
//   ]
// }