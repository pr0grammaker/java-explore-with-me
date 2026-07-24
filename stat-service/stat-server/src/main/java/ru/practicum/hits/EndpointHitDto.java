package ru.practicum.hits;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHitDto {

    @NotBlank(message = "Название сервиса не должно быть пустым")
    private String app;   // Название сервиса

    @NotBlank(message = "URI не должен быть пустым")
    @Pattern(
            regexp = "^/[a-zA-Z0-9/_-]+$",
            message = "URI должен начинаться с '/' и содержать допустимые символы"
    )
    private String uri;   // URI эндпоинта

    @NotBlank(message = "IP-адрес не должен быть пустым")
    @Pattern(
            regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$",
            message = "IP-адрес должен быть в формате IPv4"
    )
    private String ip;    // IP пользователя

    @NotNull(message = "Дата и время запроса не могут быть null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp; // В формате "yyyy-MM-dd HH:mm:ss"
}
