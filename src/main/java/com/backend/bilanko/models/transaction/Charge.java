package com.backend.bilanko.models.transaction;

import com.backend.bilanko.models.person.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import com.backend.bilanko.models.BaseEntity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Charge extends BaseEntity {

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String supplier;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(optional = false)
    private User user;
}
