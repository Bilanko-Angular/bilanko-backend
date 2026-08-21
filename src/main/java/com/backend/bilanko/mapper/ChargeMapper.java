package com.backend.bilanko.mapper;

import com.backend.bilanko.DTO.concept.transaction.ChargeResponseDTO;
import com.backend.bilanko.models.transaction.Charge;


public final class ChargeMapper {

    private ChargeMapper() {
    }

    public static ChargeResponseDTO toDto(Charge charge) {
        return new ChargeResponseDTO(
                charge.getId(),
                charge.getLabel(),
                charge.getSupplier(),
                charge.getAmount(),
                charge.getDate()
        );
    }
}
