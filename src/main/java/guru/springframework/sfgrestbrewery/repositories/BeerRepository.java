package guru.springframework.sfgrestbrewery.repositories;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import guru.springframework.sfgrestbrewery.domain.Beer;

public interface BeerRepository extends ReactiveCrudRepository<Beer, UUID> {
  // Page<Beer> findAllByBeerName(String beerName, Pageable pageable);

  // Page<Beer> findAllByBeerStyle(BeerStyleEnum beerStyle, Pageable pageable);

  // Page<Beer> findAllByBeerNameAndBeerStyle(String beerName, BeerStyleEnum
  // beerStyle, Pageable pageable);

  Beer findByUpc(String upc);
}
