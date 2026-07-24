package ru.practicum.hits;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStats {

    @NotBlank(message = "Название сервиса не должно быть пустым")
    private String app;

    @NotBlank(message = "URI не должен быть пустым")
    @Pattern(
            regexp = "^/[a-zA-Z0-9/_-]+$",
            message = "URI должен начинаться с '/' и содержать только буквы, цифры, '-', '_' и '/'"
    )
    private String uri;   // URI эндпоинта (например, /events/1)

    @NotNull(message = "Количество обращений не может быть null")
    @PositiveOrZero(message = "Количество обращений должно быть >= 0")
    private Long hits;    // Количество обращений
}

