package com.fairshare.distributed_expense_splitter.entity;

import org.openapitools.model.ExpenseSplitDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
// @Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "expense", "user" })
@EqualsAndHashCode(exclude = { "expense", "user" })
@Table(name = "expense_splits")
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The borrower

    @Column(name = "share_in_cents", nullable = false)
    private Double shareInCents; // How much this specific user owes

    public static ExpenseSplitDTO fromEntity(ExpenseSplit split) {
        ExpenseSplitDTO ret = new ExpenseSplitDTO();
        ret.setUserId(split.getUser().getId());
        ret.setAmount(split.getShareInCents());
        return ret;
    }

}