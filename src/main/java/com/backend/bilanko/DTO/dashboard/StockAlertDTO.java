package com.backend.bilanko.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockAlertDTO {
    private String name;
    private String status;
    private int qty;
    /** "low" = critique (rouge), "mid" = avertissement (orange) */
    private String level;
}
