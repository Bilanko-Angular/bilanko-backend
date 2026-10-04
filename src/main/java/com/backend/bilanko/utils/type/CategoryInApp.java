package com.backend.bilanko.utils.type;

import java.util.List;

public record CategoryInApp(
        String name,
        List<Long> idElements
) {
    public CategoryInApp {
        idElements = (idElements == null) ? List.of() : idElements;
    }
}
