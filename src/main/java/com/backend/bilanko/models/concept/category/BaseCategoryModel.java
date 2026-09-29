package com.backend.bilanko.models.concept.category;

import com.backend.bilanko.models.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseCategoryModel extends BaseEntity {
    @Column(nullable = false)
    String name;
}
