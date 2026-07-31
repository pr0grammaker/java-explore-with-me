package ru.practicum.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {


    @Query("SELECT e FROM Event e WHERE e.initiator.id = :userId")
    List<Event> findAllByUserId(@Param("userId") Long userId, Pageable pageable);


    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "     OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND ( ( :rangeStart IS NOT NULL AND :rangeEnd IS NOT NULL AND e.eventDate BETWEEN :rangeStart AND :rangeEnd ) " +
            "   OR ( :rangeStart IS NULL AND :rangeEnd IS NULL AND e.eventDate > CURRENT_TIMESTAMP ) )")
    Page<Event> findAllWithFilters(@Param("text") String text,
                                   @Param("categories") List<Long> categories,
                                   @Param("paid") Boolean paid,
                                   @Param("rangeStart") LocalDateTime rangeStart,
                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                   Pageable pageable);

}
