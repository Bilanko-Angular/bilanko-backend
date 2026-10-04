package com.backend.bilanko.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentUserDTO {
    private String name;
    private String email;
    private String date;
}
