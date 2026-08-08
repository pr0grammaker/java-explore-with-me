package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.validator.ValidIp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHitDto {

    @NotBlank(message = "Название сервиса не должно быть пустым")
    private String app;   // Название сервиса

    @NotBlank(message = "URI не должен быть пустым")
    private String uri;   // URI эндпоинта

    @NotBlank(message = "IP-адрес не должен быть пустым")
    @ValidIp
    private String ip;    // IP пользователя

    @NotNull(message = "Дата и время запроса не могут быть null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp; // В формате "yyyy-MM-dd HH:mm:ss"
}
