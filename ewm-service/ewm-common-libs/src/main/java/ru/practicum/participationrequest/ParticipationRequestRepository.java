package ru.practicum.participationrequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.RequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {


    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.requester.id = :userId " +
            "AND r.event.initiator.id <> :userId")
    List<ParticipationRequest> findUserRequestsForOtherEvents(@Param("userId") Long userId);

    @Query("SELECT r FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId " +
            "AND r.event.initiator.id = :userId")
    List<ParticipationRequest> findRequestsByUserEvent(@Param("userId") Long userId,
                                                       @Param("eventId") Long eventId);

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    long countByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

}

