package guru.springframework.sfgrestbrewery.web.controller;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 2019-04-20.
 */
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@RestController
public class BeerController {

  private static final Integer DEFAULT_PAGE_NUMBER = 0;
  private static final Integer DEFAULT_PAGE_SIZE = 25;

  private final BeerService beerService;

  @GetMapping(produces = { "application/json" }, path = "beer")
  public Mono<ResponseEntity<BeerPagedList>> listBeers(
      @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "beerName", required = false) final String beerName,
      @RequestParam(value = "beerStyle", required = false) final BeerStyleEnum beerStyle,
      @RequestParam(value = "showInventoryOnHand", required = false) Boolean showInventoryOnHand) {

    if (showInventoryOnHand == null) {
      showInventoryOnHand = false;
    }

    if (pageNumber == null || pageNumber < 0) {
      pageNumber = DEFAULT_PAGE_NUMBER;
    }

    if (pageSize == null || pageSize < 1) {
      pageSize = DEFAULT_PAGE_SIZE;
    }

    return beerService.listBeers(beerName, beerStyle, PageRequest.of(pageNumber, pageSize), showInventoryOnHand)
      .map(ResponseEntity::ok);
  }

  @GetMapping("beer/{beerId}")
  public Mono<ResponseEntity<BeerDto>> getBeerById(
      @PathVariable("beerId") final UUID beerId,
      @RequestParam(value = "showInventoryOnHand", required = false) Boolean showInventoryOnHand) {
    if (showInventoryOnHand == null) {
      showInventoryOnHand = false;
    }

    return beerService.getById(beerId, showInventoryOnHand)
      .map(ResponseEntity::ok)
      .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping("beerUpc/{upc}")
  public Mono<ResponseEntity<BeerDto>> getBeerByUpc(@PathVariable("upc") final String upc) {
    return beerService.getByUpc(upc).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping(path = "beer")
  public Mono<ResponseEntity<Void>> saveNewBeer(@RequestBody @Validated final BeerDto beerDto) {
    return beerService.saveNewBeer(beerDto)
      .map(
        savedBeer -> ResponseEntity
          .created(
            UriComponentsBuilder
              .fromHttpUrl("http://api.springframework.guru/api/v1/beer/" + savedBeer.getId().toString())
              .build()
              .toUri()
          )
          .build()
      );
  }

  @PutMapping("beer/{beerId}")
  public Mono<ResponseEntity<Void>> updateBeerById(
      @PathVariable("beerId") final UUID beerId,
      @RequestBody @Validated final BeerDto beerDto) {
    return beerService.updateBeer(beerId, beerDto)
      .map(updatedBeer -> ResponseEntity.noContent().<Void>build())
      .defaultIfEmpty(ResponseEntity.notFound().<Void>build());
  }

  @DeleteMapping("beer/{beerId}")
  public Mono<ResponseEntity<Void>> deleteBeerById(@PathVariable("beerId") final UUID beerId) {
    return beerService.getById(beerId, false)
      .flatMap(
        existingBeer -> beerService.deleteBeerById(beerId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
      )
      .defaultIfEmpty(ResponseEntity.notFound().<Void>build());
  }
}
