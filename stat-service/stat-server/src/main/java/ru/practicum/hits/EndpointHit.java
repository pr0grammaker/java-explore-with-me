package ru.practicum.hits;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String app; // Имя сервиса, например "ewm-main-service"

    @Column(nullable = false)
    private String uri; // URI, к которому был запрос

    @Column(nullable = false)
    private String ip; // IP-адрес пользователя

    @Column(nullable = false)
    private LocalDateTime timestamp; // Дата и время запроса
}
