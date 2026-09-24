package com.backend.bilanko.models.object.document;

import com.backend.bilanko.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "constances_metier", uniqueConstraints = {
        @UniqueConstraint(name = "uk_constance_type", columnNames = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstanceMetier extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private TypeConstanceMetier type;

    @Column(nullable = false)
    private String valeur;
}
