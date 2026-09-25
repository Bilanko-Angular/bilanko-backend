package com.backend.bilanko.models.transaction;

import com.backend.bilanko.models.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChargeCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

}
