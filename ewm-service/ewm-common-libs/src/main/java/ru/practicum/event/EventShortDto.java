package ru.practicum.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShortDto {
    @NotBlank(message = "Аннотация события не может быть пустой")
    @Size(max = 500, message = "Аннотация события не должна превышать 500 символов")
    private String annotation;

    @NotNull(message = "Категория события обязательна")
    private Long category;

    @Size(max = 2000, message = "Описание события не должно превышать 2000 символов")
    private String description;

    @NotNull(message = "Дата проведения события обязательна")
    @Future(message = "Дата проведения должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Локация события обязательна")
    private Location location;

    @NotNull(message = "Поле 'paid' обязательно")
    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Long participantLimit = 0L;

    @NotNull(message = "Поле 'requestModeration' обязательно")
    private Boolean requestModeration = true;

    @NotBlank(message = "Заголовок события обязателен")
    @Size(min = 4, max = 50, message = "Заголовок должен содержать от 4 до 50 символов")
    private String title;
}
