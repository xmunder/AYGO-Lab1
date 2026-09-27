package co.edu.escuelaing.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ArrivalGatewayControllerTest {

    @Test
    void shouldReturnServiceResponseWhenPostArrivalIsForwarded() throws IOException {
        // Arrange
        AtomicReference<String> method = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        try (TestServer server = new TestServer(201, "created", method, body)) {
            ArrivalGatewayController controller = new ArrivalGatewayController(
                    HttpClient.newHttpClient(), server.url());

            // Act
            var response = controller.create("{\"name\":\"Alice\"}");

            // Assert
            assertEquals(201, response.getStatusCode().value());
            assertEquals("created", response.getBody());
            assertEquals("POST", method.get());
            assertEquals("{\"name\":\"Alice\"}", body.get());
        }
    }

    @Test
    void shouldReturnArrivalListWhenGetArrivalsIsForwarded() throws IOException {
        // Arrange
        AtomicReference<String> method = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        try (TestServer server = new TestServer(200, "[]", method, body)) {
            ArrivalGatewayController controller = new ArrivalGatewayController(
                    HttpClient.newHttpClient(), server.url());

            // Act
            var response = controller.list();

            // Assert
            assertEquals(200, response.getStatusCode().value());
            assertEquals("[]", response.getBody());
            assertEquals("GET", method.get());
            assertEquals("", body.get());
        }
    }

    @Test
    void shouldReturnBadGatewayWhenServiceIsUnavailable() {
        // Arrange
        ArrivalGatewayController controller = new ArrivalGatewayController(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(100)).build(),
                "http://127.0.0.1:1");

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                controller::list);

        // Assert
        assertEquals(502, exception.getStatusCode().value());
    }

    @Test
    void shouldRestoreInterruptFlagWhenRequestIsInterrupted() throws Exception {
        // Arrange
        HttpClient client = mock(HttpClient.class);
        doThrow(new InterruptedException()).when(client)
                .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        ArrivalGatewayController controller = new ArrivalGatewayController(client, "http://arrival-service");

        // Act
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                controller::list);

        // Assert
        assertEquals(502, exception.getStatusCode().value());
        assertTrue(Thread.interrupted());
    }

    @Test
    void shouldCreateControllerWhenDefaultConfigurationIsAvailable() {
        // Arrange

        // Act
        ArrivalGatewayController controller = new ArrivalGatewayController();

        // Assert
        assertNotNull(controller);
    }

    private static final class TestServer implements AutoCloseable {
        private final HttpServer server;

        private TestServer(int status, String response, AtomicReference<String> method,
                AtomicReference<String> body) throws IOException {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/api/arrivals", exchange -> {
                method.set(exchange.getRequestMethod());
                body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();
        }

        private String url() {
            return URI.create("http://localhost:" + server.getAddress().getPort()).toString();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
