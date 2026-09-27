package co.edu.escuelaing.arrivalservice;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "arrivals")
public record Arrival(
        @Id String id,
        String name,
        Instant timestamp) {
}
