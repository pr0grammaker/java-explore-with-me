package ru.practicum.compilation;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {

    private List<@NotNull @Positive Long> events;

    @NotNull(message = "Статус подборки обязателен")
    private Boolean pinned = false;

    @NotBlank(message = "Заголовок обязателен")
    @Size(min = 1, max = 50, message = "Заголовок должен содержать от 1 до 50 символов")
    private String title;
}

