package guru.springframework.sfgrestbrewery.services;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.empty;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.repositories.BeerRepository;
import guru.springframework.sfgrestbrewery.web.controller.NotFoundException;
import guru.springframework.sfgrestbrewery.web.mappers.BeerMapper;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 2019-04-20.
 */
@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {
  private final BeerRepository beerRepository;
  private final BeerMapper beerMapper;
  private final R2dbcEntityTemplate template;

  @Cacheable(cacheNames = "beerListCache", condition = "#showInventoryOnHand == false ")
  @Override
  public Mono<BeerPagedList> listBeers(
      final String beerName,
      final BeerStyleEnum beerStyle,
      final PageRequest pageRequest,
      final Boolean showInventoryOnHand) {
    Query query = null;

    if (StringUtils.hasLength(beerName) && beerStyle != null) {
      // search both
      query = query(where("beerName").is(beerName).and("beerStyle").is(beerStyle));
    } else if (StringUtils.hasLength(beerName) && beerStyle == null) {
      // search beer_service name
      query = query(where("beerName").is(beerName));
    } else if (!StringUtils.hasLength(beerName) && beerStyle != null) {
      // search beer_service style
      query = query(where("beerStyle").is(beerStyle));
    } else {
      query = empty();
    }

    return template.select(Beer.class)
      .matching(query.with(pageRequest))
      .all()
      .map(beerMapper::beerToBeerDto)
      .collect(Collectors.toList())
      .map(
        beers -> new BeerPagedList(
          beers,
          PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize()),
          beers.size()
        )
      );
  }

  @Cacheable(cacheNames = "beerCache", key = "#beerId", condition = "#showInventoryOnHand == false ")
  @Override
  public Mono<BeerDto> getById(final UUID beerId, final Boolean showInventoryOnHand) {
    if (Boolean.TRUE.equals(showInventoryOnHand)) {
      return beerRepository.findById(beerId).map(beerMapper::beerToBeerDtoWithInventory);
    }
    return beerRepository.findById(beerId).map(beerMapper::beerToBeerDto);
  }

  @Override
  public BeerDto saveNewBeer(final BeerDto beerDto) {
    return beerMapper.beerToBeerDto(beerRepository.save(beerMapper.beerDtoToBeer(beerDto)).block());
  }

  @Override
  public BeerDto updateBeer(final UUID beerId, final BeerDto beerDto) {
    final var beer = beerRepository.findById(beerId).blockOptional().orElseThrow(NotFoundException::new);

    beer.setBeerName(beerDto.getBeerName());
    beer.setBeerStyle(BeerStyleEnum.valueOf(beerDto.getBeerStyle()));
    beer.setPrice(beerDto.getPrice());
    beer.setUpc(beerDto.getUpc());

    return beerMapper.beerToBeerDto(beerRepository.save(beer).block());
  }

  @Cacheable(cacheNames = "beerUpcCache")
  @Override
  public Mono<BeerDto> getByUpc(final String upc) {
    return beerRepository.findByUpc(upc).map(beerMapper::beerToBeerDto);
  }

  @Override
  public void deleteBeerById(final UUID beerId) {
    beerRepository.deleteById(beerId);
  }
}
