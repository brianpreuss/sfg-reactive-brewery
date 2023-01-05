package guru.springframework.sfgrestbrewery.repositories;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import guru.springframework.sfgrestbrewery.domain.Beer;
import reactor.core.publisher.Mono;

public interface BeerRepository extends ReactiveCrudRepository<Beer, UUID> {
  Mono<Beer> findByUpc(String upc);
}
