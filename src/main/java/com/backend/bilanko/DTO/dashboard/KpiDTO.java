package com.backend.bilanko.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KpiDTO {
    private String label;
    private String value;
    private String icon;
    private String trend;
    private boolean trendPositive;
}
