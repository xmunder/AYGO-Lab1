package co.edu.escuelaing.arrivalservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ArrivalControllerTest {

    @Mock
    private ArrivalRepository repository;

    private ArrivalController controller;

    @BeforeEach
    void setUp() {
        controller = new ArrivalController(repository);
    }

    @Test
    void shouldCreateTrimmedArrivalWithTimestampWhenNameContainsWhitespace() {
        // Arrange
        Arrival saved = new Arrival("id", "Alice", Instant.now());
        when(repository.save(any(Arrival.class))).thenReturn(saved);

        // Act
        Arrival result = controller.create(new ArrivalRequest("  Alice  "));

        // Assert
        assertEquals(saved, result);
        ArgumentCaptor<Arrival> captor = ArgumentCaptor.forClass(Arrival.class);
        verify(repository).save(captor.capture());
        assertEquals("Alice", captor.getValue().name());
        assertNotNull(captor.getValue().timestamp());
    }

    @Test
    void shouldThrowBadRequestWhenNameIsBlank() {
        // Arrange
        ArrivalRequest request = new ArrivalRequest("   ");

        // Act
        assertThrows(ResponseStatusException.class,
                () -> controller.create(request));
    }

    @Test
    void shouldThrowBadRequestWhenNameIsNull() {
        // Arrange
        ArrivalRequest request = new ArrivalRequest(null);

        // Act
        assertThrows(ResponseStatusException.class,
                () -> controller.create(request));
    }

    @Test
    void shouldReturnAllArrivalsWhenArrivalsExist() {
        // Arrange
        List<Arrival> arrivals = List.of(
                new Arrival("one", "Alice", Instant.now()),
                new Arrival("two", "Bob", Instant.now()));
        when(repository.findAll()).thenReturn(arrivals);

        // Act
        List<Arrival> result = controller.list();

        // Assert
        assertEquals(arrivals, result);
        verify(repository).findAll();
    }
}
