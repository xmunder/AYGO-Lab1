package co.edu.escuelaing.arrivalservice;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArrivalRepository extends MongoRepository<Arrival, String> {
}
