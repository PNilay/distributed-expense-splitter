package com.fairshare.distributed_expense_splitter.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.openapitools.model.GroupStatus;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "groups")
public class Group {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String description;

  @ManyToOne
  @JoinColumn(name = "created_by")
  private User createdBy;

  @Enumerated(EnumType.STRING)
  private GroupStatus status;

  @CreationTimestamp
  private OffsetDateTime createdAt;

  @ManyToMany
  @JoinTable(
    name = "group_members",
    joinColumns = @JoinColumn(name = "group_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<User> members = new HashSet<>();
}
