package ru.practicum.hits;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
}
