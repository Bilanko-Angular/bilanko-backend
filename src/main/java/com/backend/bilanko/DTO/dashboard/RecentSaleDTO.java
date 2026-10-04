package com.backend.bilanko.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentSaleDTO {
    private long id;
    private String date;
    private String client;
    private int items;
    private double total;
    private double margin;
}
