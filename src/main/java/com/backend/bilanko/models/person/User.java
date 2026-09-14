package com.backend.bilanko.models.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import com.backend.bilanko.models.BaseEntity;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User extends BaseEntity implements UserDetails {
    @Column(nullable = false)
    private String name;
    private String subname;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    @JsonIgnore // <-- important : jamais renvoyer le hash en JSON
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column
    private String profilePictureUrl;
    @Builder.Default
    @Embedded
    private AppearancePreferences appearancePreferences = new AppearancePreferences();
    @Builder.Default
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    // Pas de "nullable = false" : null par défaut tant que l'utilisateur ne l'a pas renseigné
    @Column(name = "phone_number")
    private String phoneNumber;

    // Vide par défaut — correspond à la raison sociale côté documents
    @Builder.Default
    @Column(name = "company_name")
    private String companyName = "";

    /** Activité commerciale du commerçant (nullable jusqu'à renseignement). */
    @Column(name = "activite")
    private String activite;

    /** Numéro d'identification unique (NIU / NUI). */
    @Column(name = "niu")
    private String niu;

    /** Adresse du commerce. */
    @Column(name = "adresse")
    private String adresse;

    /** Date de création de l'activité (ISO yyyy-MM-dd ou libellé saisi). */
    @Column(name = "date_de_creation_activite")
    private String dateDeCreationActivite;

    // Préférences de notifications, embarquées dans la table users
    @Builder.Default
    @Embedded
    private NotificationPreferences notificationPreferences = new NotificationPreferences();

    // --- Méthodes requises par UserDetails ---
    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }

}