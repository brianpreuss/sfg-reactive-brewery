package guru.springframework.sfgrestbrewery.services;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;

import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 2019-04-20.
 */
public interface BeerService {
  Mono<BeerPagedList> listBeers(
      String beerName,
      BeerStyleEnum beerStyle,
      PageRequest pageRequest,
      Boolean showInventoryOnHand);

  Mono<BeerDto> getById(UUID beerId, Boolean showInventoryOnHand);

  BeerDto saveNewBeer(BeerDto beerDto);

  BeerDto updateBeer(UUID beerId, BeerDto beerDto);

  Mono<BeerDto> getByUpc(String upc);

  void deleteBeerById(UUID beerId);
}
