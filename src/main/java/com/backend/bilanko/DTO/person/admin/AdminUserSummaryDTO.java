package com.backend.bilanko.DTO.person.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserSummaryDTO {
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long newUsersThisMonth;
}
