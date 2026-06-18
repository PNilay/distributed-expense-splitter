package com.fairshare.distributed_expense_splitter.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import org.hibernate.annotations.CreationTimestamp;
import org.openapitools.model.GroupDTO;
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

  private String icon;

  @ManyToOne
  @JoinColumn(name = "created_by")
  private User createdBy;

  @Enumerated(EnumType.STRING)
  private GroupStatus status;

  @CreationTimestamp
  private OffsetDateTime createdAt;

  @ManyToMany
  @JoinTable(name = "group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  @Singular
  private Set<User> members = new HashSet<>();

  public boolean hasMember(Long memberId) {
    return this.members != null && this.members.stream().anyMatch(mem -> mem.getId().equals(memberId));
  }

  public int getTotalMemberCount() {
    return this.members == null ? 0 : this.members.size();
  }

  public static GroupDTO fromEntity(Group group) {
    if (group == null)
      return null;

    GroupDTO dto = new GroupDTO();
    dto.setId(group.getId());
    dto.setName(group.getName());
    dto.setDescription(group.getDescription());
    dto.setStatus(group.getStatus());
    dto.setCreatedAt(group.getCreatedAt());
    dto.setIcon(group.getIcon());
    if (group.getCreatedBy() != null) {
      dto.setCreatedBy(group.getCreatedBy().getId());
    }

    return dto;
  }
}
