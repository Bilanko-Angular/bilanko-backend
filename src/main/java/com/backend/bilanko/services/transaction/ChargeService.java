package com.backend.bilanko.services.transaction;

import com.backend.bilanko.DTO.concept.transaction.ChargeRequestDTO;
import com.backend.bilanko.DTO.concept.transaction.ChargeResponseDTO;
import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.mapper.ChargeMapper;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.concept.transaction.ChargeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChargeService {

    private final ChargeRepository chargeRepository;

    @Transactional
    public ChargeResponseDTO createCharge(ChargeRequestDTO dto, User currentUser) {
        Charge charge = Charge.builder()
                .label(dto.label())
                .supplier(dto.supplier())
                .amount(dto.amount())
                .date(dto.date())
                .user(currentUser)
                .build();

        Charge saved = chargeRepository.save(charge);
        return ChargeMapper.toDto(saved);
    }

    public List<ChargeResponseDTO> getAllCharges(User currentUser) {
        return chargeRepository.findByUserOrderByDateDesc(currentUser)
                .stream()
                .map(ChargeMapper::toDto)
                .toList();
    }

    public ChargeResponseDTO getChargeById(long id, User currentUser) {
        Charge charge = findOwnedCharge(id, currentUser);
        return ChargeMapper.toDto(charge);
    }

    @Transactional
    public ChargeResponseDTO updateCharge(long id, ChargeRequestDTO dto, User currentUser) {
        Charge charge = findOwnedCharge(id, currentUser);

        charge.setLabel(dto.label());
        charge.setSupplier(dto.supplier());
        charge.setAmount(dto.amount());
        charge.setDate(dto.date());

        Charge saved = chargeRepository.save(charge);
        return ChargeMapper.toDto(saved);
    }

    @Transactional
    public void deleteCharge(long id, User currentUser) {
        Charge charge = findOwnedCharge(id, currentUser);
        chargeRepository.delete(charge);
    }

    private Charge findOwnedCharge(long id, User currentUser) {
        return chargeRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Charge introuvable : id=" + id
                ));
    }
}
