package com.backend.bilanko.models.transaction;

import com.backend.bilanko.models.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChargeCategory extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "chargeCategory")
    private List<Charge> charges = new ArrayList<>();
}
