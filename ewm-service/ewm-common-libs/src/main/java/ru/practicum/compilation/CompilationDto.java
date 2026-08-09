package ru.practicum.compilation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.event.EventCompilationDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {

    private List<@Valid EventCompilationDto> events;

    @NotNull(message = "Идентификатор обязателен")
    private Long id;

    @NotNull(message = "Поле pinned обязательно")
    private Boolean pinned;

    @NotBlank(message = "Заголовок обязателен")
    @Size(min = 1, max = 50, message = "Заголовок должен содержать от 1 до 50 символов")
    private String title;
}

