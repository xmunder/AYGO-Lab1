package co.edu.escuelaing.gateway;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/arrivals")
public class ArrivalGatewayController {

    private final HttpClient client;
    private final String serviceUrl;

    public ArrivalGatewayController() {
        this(HttpClient.newHttpClient(),
                System.getenv().getOrDefault("ARRIVAL_SERVICE_URL", "http://localhost:8081"));
    }

    ArrivalGatewayController(HttpClient client, String serviceUrl) {
        this.client = Objects.requireNonNull(client);
        this.serviceUrl = Objects.requireNonNull(serviceUrl);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> create(@RequestBody String body) {
        return forward("POST", body);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> list() {
        return forward("GET", null);
    }

    private ResponseEntity<String> forward(String method, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(serviceUrl + "/api/arrivals"))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            HttpRequest request = "POST".equals(method)
                    ? builder.POST(HttpRequest.BodyPublishers.ofString(body)).build()
                    : builder.GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(response.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Arrival service request was interrupted", exception);
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Arrival service is unavailable", exception);
        }
    }
}
