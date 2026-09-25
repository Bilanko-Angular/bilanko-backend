package com.backend.bilanko.DTO.person.admin;

import com.backend.bilanko.models.person.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponseDTO {
    private long id;
    private String name;
    private String subname;
    private String email;
    private String phoneNumber;
    private Role role;
    private String adresse;
    private boolean active;
    private Instant lastConnectionDate;
    private int numberOfProducts;
}
