package mock.participationrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.PrivateAppExploreWithMe;
import ru.practicum.event.EventRequestStatusUpdateRequest;
import ru.practicum.event.EventRequestStatusUpdateResult;
import ru.practicum.event.RequestStatus;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.participationrequest.ParticipationRequestController;
import ru.practicum.participationrequest.ParticipationRequestDto;
import ru.practicum.participationrequest.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ParticipationRequestController.class)
@ContextConfiguration(classes = PrivateAppExploreWithMe.class)
public class ParticipationRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipationRequestService participationRequestService;

    private ParticipationRequestDto participationRequestDto;
    private ParticipationRequestDto updateParticipationRequestDto;
    private EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest;
    private EventRequestStatusUpdateResult eventRequestStatusUpdateResult;

    @BeforeEach
    void setup() {
        participationRequestDto = ParticipationRequestDto.builder()
                .id(100L)
                .event(1L)
                .requester(5L)
                .status(String.valueOf(RequestStatus.PENDING))
                .created(LocalDateTime.now())
                .build();

        updateParticipationRequestDto = ParticipationRequestDto.builder()
                .id(100L)
                .event(1L)
                .requester(5L)
                .status(String.valueOf(RequestStatus.CANCELED))
                .created(LocalDateTime.now())
                .build();

        eventRequestStatusUpdateRequest = new EventRequestStatusUpdateRequest();
        eventRequestStatusUpdateRequest.setRequestIds(List.of(100L));
        eventRequestStatusUpdateRequest.setStatus(RequestStatus.CONFIRMED);

        eventRequestStatusUpdateResult = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(participationRequestDto))
                .rejectedRequests(List.of())
                .build();
    }

    @Test
    void createParticipationRequest_ReturnsDto() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(participationRequestService.createParticipationRequest(userId, eventId))
                .thenReturn(participationRequestDto);

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(participationRequestDto.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(participationRequestService, times(1)).createParticipationRequest(userId, eventId);
    }

    @Test
    void createParticipationRequest_UserNotFound_ThrowsException() throws Exception {
        Long userId = 999L;
        Long eventId = 1L;

        when(participationRequestService.createParticipationRequest(userId, eventId))
                .thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 999 не найден"));
    }

    @Test
    void createParticipationRequest_EventNotFound_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 999L;

        when(participationRequestService.createParticipationRequest(userId, eventId))
                .thenThrow(new NotFoundException("Событие с id = 999 не найдено"));

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Событие с id = 999 не найдено"));
    }

    @Test
    void createParticipationRequest_DuplicateRequest_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(participationRequestService.createParticipationRequest(userId, eventId))
                .thenThrow(new InvalidEventOperationException("Заявка уже существует"));

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Заявка уже существует"));
    }

    @Test
    void createParticipationRequest_InitiatorCannotRequest_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(participationRequestService.createParticipationRequest(userId, eventId))
                .thenThrow(new InvalidEventOperationException("Инициатор не может подать заявку на своё событие"));

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Инициатор не может подать заявку на своё событие"));
    }

    @Test
    void createParticipationRequest_EventNotPublished_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(participationRequestService.createParticipationRequest(userId, eventId))
                .thenThrow(new InvalidEventOperationException("Нельзя участвовать в неопубликованном событии"));

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Нельзя участвовать в неопубликованном событии"));
    }

    @Test
    void createParticipationRequest_LimitReached_ThrowsException() throws Exception {
        Long userId = 5L;
        Long eventId = 1L;

        when(participationRequestService.createParticipationRequest(userId, eventId))
                .thenThrow(new InvalidEventOperationException("Лимит заявок на событие достигнут"));

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", userId, eventId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Лимит заявок на событие достигнут"));
    }

    @Test
    void getParticipationRequestsByUserId_ReturnsRequests() throws Exception {
        Long userId = 5L;

        when(participationRequestService.getParticipationRequestsByUserId(userId))
                .thenReturn(List.of(participationRequestDto));

        mockMvc.perform(get("/users/{userId}/requests", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(participationRequestDto.getId()))
                .andExpect(jsonPath("$[0].event").value(participationRequestDto.getEvent()))
                .andExpect(jsonPath("$[0].requester").value(participationRequestDto.getRequester()))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(participationRequestService, times(1)).getParticipationRequestsByUserId(userId);
    }

    @Test
    void getParticipationRequestsByUserId_UserNotFound_ThrowsException() throws Exception {
        Long userId = 999L;

        when(participationRequestService.getParticipationRequestsByUserId(userId))
                .thenThrow(new NotFoundException("Пользователь с id = 999 не найден"));

        mockMvc.perform(get("/users/{userId}/requests", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id = 999 не найден"));

        verify(participationRequestService, times(1)).getParticipationRequestsByUserId(userId);
    }

    @Test
    void getParticipationRequestsByUserId_NoRequests_ReturnsEmptyList() throws Exception {
        Long userId = 5L;

        when(participationRequestService.getParticipationRequestsByUserId(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/users/{userId}/requests", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(participationRequestService, times(1)).getParticipationRequestsByUserId(userId);
    }

    @Test
    void updateParticipationRequest_ReturnsDto() throws Exception {
        Long userId = 5L;
        Long requestId = 100L;

        when(participationRequestService.updateParticipationRequest(userId, requestId))
                .thenReturn(updateParticipationRequestDto);

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updateParticipationRequestDto.getId()))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(participationRequestService, times(1)).updateParticipationRequest(userId, requestId);
    }

    @Test
    void updateParticipationRequest_UserNotFound_ThrowsException() throws Exception {
        Long userId = 999L;
        Long requestId = 100L;

        when(participationRequestService.updateParticipationRequest(userId, requestId))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id=999 не найден"));

        verify(participationRequestService, times(1)).updateParticipationRequest(userId, requestId);
    }

    @Test
    void updateParticipationRequest_RequestNotFound_ThrowsException() throws Exception {
        Long userId = 5L;
        Long requestId = 999L;

        when(participationRequestService.updateParticipationRequest(userId, requestId))
                .thenThrow(new NotFoundException("Запрос на участие с id = 999 не был найден"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Запрос на участие с id = 999 не был найден"));

        verify(participationRequestService, times(1)).updateParticipationRequest(userId, requestId);
    }

    @Test
    void updateParticipationRequest_RequestBelongsToAnotherUser_ThrowsException() throws Exception {
        Long userId = 5L;
        Long requestId = 100L;

        when(participationRequestService.updateParticipationRequest(userId, requestId))
                .thenThrow(new InvalidEventOperationException("Заявка id=100 не принадлежит пользователю id=5"));

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", userId, requestId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Заявка id=100 не принадлежит пользователю id=5"));

        verify(participationRequestService, times(1)).updateParticipationRequest(userId, requestId);
    }


}

