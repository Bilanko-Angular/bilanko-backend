package com.backend.bilanko.models.object.product;
import com.backend.bilanko.models.person.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String name;
    @Min(0)
    private int quantity;
    @Min(0)
    private double price;
    @Min(0)
    private double purchasePrice;
    @ManyToMany
    private List<Category> categories;
    @ManyToOne
    @JsonIgnore
    private User user;
    // New fields
    @Column(nullable = false, unique = true)
    private String reference;
    private Integer alertThreshold;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}