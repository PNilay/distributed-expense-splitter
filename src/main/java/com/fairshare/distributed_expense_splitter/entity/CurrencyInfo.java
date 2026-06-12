package com.fairshare.distributed_expense_splitter.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "currencies")
public class CurrencyInfo {

  @Id
  @Column(length = 3)
  private String code; // 'USD', 'EUR', 'JPY'

  @Column(nullable = false, length = 8)
  private String symbol; // '$', '€', '¥'

  @Column(nullable = false, length = 50)
  private String name; // 'US Dollar'

  @Column(name = "decimal_places", nullable = false)
  private int decimalPlaces; // 2 for USD, 0 for JPY
}
