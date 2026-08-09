package integration.participationrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.PrivateAppExploreWithMe;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.event.*;
import ru.practicum.exceptions.InvalidEventOperationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.participationrequest.ParticipationRequest;
import ru.practicum.participationrequest.ParticipationRequestDto;
import ru.practicum.participationrequest.ParticipationRequestRepository;
import ru.practicum.participationrequest.ParticipationRequestService;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PrivateAppExploreWithMe.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("ci")
public class PrivateAppExploreWithMeParticipationRequestServiceTest {

    @Autowired
    private ParticipationRequestRepository participationRequestRepository;

    @Autowired
    private ParticipationRequestService participationRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private User otherUser;

    private Event event;
    private Event pulishedEvent;

    @BeforeEach
    void setup() {
        user = userRepository.save(User.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build());

        otherUser = userRepository.save(User.builder()
                .name("Пётр Петров")
                .email("petr@example.com")
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Концерт")
                .build());

        event = eventRepository.save(Event.builder()
                .annotation("Аннотация")
                .description("Описание")
                .title("Событие")
                .category(category)
                .eventDate(LocalDateTime.now().plusDays(3))
                .location(new Location(51.1694f, 71.4491f))
                .initiator(otherUser)
                .paid(true)
                .participantLimit(10L)
                .requestModeration(true)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build());

        pulishedEvent = eventRepository.save(Event.builder()
                .annotation("Аннотация")
                .description("Описание")
                .title("Событие")
                .category(category)
                .eventDate(LocalDateTime.now().plusDays(3))
                .location(new Location(51.1694f, 71.4491f))
                .initiator(otherUser)
                .paid(true)
                .participantLimit(10L)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .build());
    }

    @Test
    void getParticipationRequestsByUserId_ReturnsRequests() {
        ParticipationRequest request = participationRequestRepository.save(ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build());

        Collection<ParticipationRequestDto> result =
                participationRequestService.getParticipationRequestsByUserId(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo(request.getId());
    }

    @Test
    void getParticipationRequestsByUserId_UserNotFound_ThrowsException() {
        assertThatThrownBy(() -> participationRequestService.getParticipationRequestsByUserId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id = 999 не найден");
    }

    @Test
    void getParticipationRequestsByUserId_NoRequests_ReturnsEmptyList() {
        Collection<ParticipationRequestDto> result =
                participationRequestService.getParticipationRequestsByUserId(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void createParticipationRequest_Success() {
        ParticipationRequestDto dto = participationRequestService.createParticipationRequest(user.getId(), pulishedEvent.getId());

        assertThat(dto.getRequester()).isEqualTo(user.getId());
        assertThat(dto.getEvent()).isEqualTo(pulishedEvent.getId());
        assertThat(dto.getStatus()).isEqualTo(String.valueOf(EventState.PENDING));
    }

    @Test
    void createParticipationRequest_UserNotFound_ThrowsException() {
        assertThatThrownBy(() -> participationRequestService.createParticipationRequest(999L, event.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id = 999 не найден");
    }

    @Test
    void createParticipationRequest_EventNotFound_ThrowsException() {
        assertThatThrownBy(() -> participationRequestService.createParticipationRequest(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Событие с id = 999 не найдено");
    }

    @Test
    void createParticipationRequest_DuplicateRequest_ThrowsException() {
        participationRequestService.createParticipationRequest(user.getId(), pulishedEvent.getId());

        assertThatThrownBy(() -> participationRequestService.createParticipationRequest(user.getId(), pulishedEvent.getId()))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Заявка уже существует");
    }

    @Test
    void createParticipationRequest_InitiatorCannotRequest_ThrowsException() {
        assertThatThrownBy(() -> participationRequestService.createParticipationRequest(otherUser.getId(), event.getId()))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Инициатор не может подать заявку на своё событие");
    }

    @Test
    void createParticipationRequest_EventNotPublished_ThrowsException() {
        event.setState(EventState.PENDING);
        eventRepository.save(event);

        assertThatThrownBy(() -> participationRequestService.createParticipationRequest(user.getId(), event.getId()))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Нельзя участвовать в неопубликованном событии");
    }

    @Test
    void createParticipationRequest_LimitReached_ThrowsException() {
        pulishedEvent.setParticipantLimit(1L);
        pulishedEvent.setRequestModeration(false); // заявки подтверждаются автоматически
        eventRepository.save(pulishedEvent);

        participationRequestService.createParticipationRequest(user.getId(), pulishedEvent.getId());

        User another = userRepository.save(User.builder()
                .name("Сергей Сергеев")
                .email("sergey@example.com")
                .build());

        assertThatThrownBy(() -> participationRequestService.createParticipationRequest(another.getId(), pulishedEvent.getId()))
                .isInstanceOf(InvalidEventOperationException.class)
                .hasMessage("Лимит заявок на событие достигнут");
    }


    @Test
    void createParticipationRequest_AutoConfirmed_WhenNoLimit() {
        pulishedEvent.setParticipantLimit(0L);
        eventRepository.save(pulishedEvent);

        ParticipationRequestDto dto = participationRequestService.createParticipationRequest(user.getId(), pulishedEvent.getId());

        assertThat(dto.getStatus()).isEqualTo(String.valueOf(RequestStatus.CONFIRMED));
    }

    @Test
    void createParticipationRequest_AutoConfirmed_WhenModerationDisabled() {
        pulishedEvent.setRequestModeration(false);
        eventRepository.save(pulishedEvent);

        ParticipationRequestDto dto = participationRequestService.createParticipationRequest(user.getId(), pulishedEvent.getId());

        assertThat(dto.getStatus()).isEqualTo(String.valueOf(RequestStatus.CONFIRMED));
    }
}
