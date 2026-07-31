package ru.practicum.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.participationrequest.ParticipationRequestDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateResult {

    // Список подтверждённых заявок
    private List<ParticipationRequestDto> confirmedRequests;

    // Список отклонённых заявок
    private List<ParticipationRequestDto> rejectedRequests;
}
