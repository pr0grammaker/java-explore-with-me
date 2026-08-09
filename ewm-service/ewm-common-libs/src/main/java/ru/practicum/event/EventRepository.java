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
            "AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CAST(CONCAT('%', :text, '%') AS text)) " +
            "     OR LOWER(e.description) LIKE LOWER(CAST(CONCAT('%', :text, '%') AS text))) " +
            "AND (CAST(:categories AS text) IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.eventDate BETWEEN :start AND :end")
    Page<Event> findAllBetween(@Param("text") String text,
                               @Param("categories") List<Long> categories,
                               @Param("paid") Boolean paid,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end,
                               Pageable pageable);


    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CAST(CONCAT('%', :text, '%') AS text)) " +
            "     OR LOWER(e.description) LIKE LOWER(CAST(CONCAT('%', :text, '%') AS text))) " +
            "AND (CAST(:categories AS text) IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.eventDate >= :start")
    Page<Event> findAllAfter(@Param("text") String text,
                             @Param("categories") List<Long> categories,
                             @Param("paid") Boolean paid,
                             @Param("start") LocalDateTime start,
                             Pageable pageable);


    @Query("SELECT e FROM Event e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CAST(CONCAT('%', :text, '%') AS text)) " +
            "     OR LOWER(e.description) LIKE LOWER(CAST(CONCAT('%', :text, '%') AS text))) " +
            "AND (CAST(:categories AS text) IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.eventDate <= :end")
    Page<Event> findAllBefore(@Param("text") String text,
                              @Param("categories") List<Long> categories,
                              @Param("paid") Boolean paid,
                              @Param("end") LocalDateTime end,
                              Pageable pageable);


    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (cast(:start as timestamp) IS NULL OR e.eventDate >= :start) " +
            "AND (cast(:end as timestamp) IS NULL OR e.eventDate <= :end)")
    Page<Event> findAllByAdminFilters(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );


    @Query("SELECT COUNT(e) > 0 FROM Event e " +
            "WHERE e.location.lat = :lat " +
            "AND e.location.lon = :lon " +
            "AND e.eventDate = :eventDate " +
            "AND e.id <> :eventId")
    boolean existsAnotherEventAtSamePlaceAndTime(@Param("lat") Float lat,
                                                 @Param("lon") Float lon,
                                                 @Param("eventDate") LocalDateTime eventDate,
                                                 @Param("eventId") Long eventId);


    boolean existsByLocationLatAndLocationLonAndEventDate(Float lat, Float lon, LocalDateTime eventDate);

    boolean existsByCategoryId(Long catId);
}
