package ru.practicum.compilation;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {

    // Список id событий для полной замены текущего списка
    private List<@NotNull @Positive Long> events;

    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Заголовок должен содержать от 1 до 50 символов")
    private String title;
}

