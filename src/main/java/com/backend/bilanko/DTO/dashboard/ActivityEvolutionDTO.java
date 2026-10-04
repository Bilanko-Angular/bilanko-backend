package com.backend.bilanko.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEvolutionDTO {
    private List<String> labels;
    private List<Double> ca;
    private List<Double> marge;
    private List<Double> charges;
}
