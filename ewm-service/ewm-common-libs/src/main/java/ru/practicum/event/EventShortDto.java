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
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов")
    private String annotation;

    @NotNull(message = "Категория события обязательна")
    private Long category;

    @NotBlank(message = "Описание события не должно быть пустым")
    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Дата проведения события обязательна")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future(message = "Дата проведения события должна быть в будущем")
    private LocalDateTime eventDate;

    @NotNull(message = "Локация события обязательна")
    private Location location;

    private Boolean paid = false;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Long participantLimit = 0L;

    private Boolean requestModeration = true;

    @NotBlank(message = "Заголовок события обязателен")
    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов")
    private String title;
}
