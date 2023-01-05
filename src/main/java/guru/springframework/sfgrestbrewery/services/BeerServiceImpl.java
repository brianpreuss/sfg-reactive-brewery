package guru.springframework.sfgrestbrewery.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 2019-04-20.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {
  private final BeerRepository beerRepository;
  private final BeerMapper beerMapper;

  @Cacheable(cacheNames = "beerListCache", condition = "#showInventoryOnHand == false ")
  @Override
  public BeerPagedList listBeers(
      final String beerName,
      final BeerStyleEnum beerStyle,
      final PageRequest pageRequest,
      final Boolean showInventoryOnHand) {

    BeerPagedList beerPagedList;
    Page<Beer> beerPage;

    if (!StringUtils.isEmpty(beerName) && !StringUtils.isEmpty(beerStyle)) {
      // search both
      beerPage = new PageImpl<>(
        List.of(new Beer(UUID.randomUUID(), 0L, beerName, beerStyle, beerName, null, null, null, null))
      );
      // beerRepository.findAllByBeerNameAndBeerStyle(beerName, beerStyle,
      // pageRequest);
    } else if (!StringUtils.isEmpty(beerName) && StringUtils.isEmpty(beerStyle)) {
      // search beer_service name
      beerPage = new PageImpl<>(
        List.of(new Beer(UUID.randomUUID(), 1L, beerName, beerStyle, beerName, null, null, null, null))
      );
      // beerRepository.findAllByBeerName(beerName, pageRequest);
    } else if (StringUtils.isEmpty(beerName) && !StringUtils.isEmpty(beerStyle)) {
      // search beer_service style
      beerPage = new PageImpl<>(
        List.of(new Beer(UUID.randomUUID(), 2L, beerName, beerStyle, beerName, null, null, null, null))
      );
      // beerRepository.findAllByBeerStyle(beerStyle, pageRequest);
    } else {
      beerPage = new PageImpl<>(
        List.of(new Beer(UUID.randomUUID(), 3L, beerName, beerStyle, beerName, null, null, null, null))
      );
      // beerRepository.findAll(pageRequest);
    }

    if (showInventoryOnHand) {
      beerPagedList = new BeerPagedList(
        beerPage.getContent().stream().map(beerMapper::beerToBeerDtoWithInventory).collect(Collectors.toList()),
        PageRequest.of(beerPage.getPageable().getPageNumber(), beerPage.getPageable().getPageSize()),
        beerPage.getTotalElements()
      );
    } else {
      beerPagedList = new BeerPagedList(
        beerPage.getContent().stream().map(beerMapper::beerToBeerDto).collect(Collectors.toList()),
        PageRequest.of(beerPage.getPageable().getPageNumber(), beerPage.getPageable().getPageSize()),
        beerPage.getTotalElements()
      );
    }

    return beerPagedList;
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
  public BeerDto getByUpc(final String upc) {
    return beerMapper.beerToBeerDto(beerRepository.findByUpc(upc));
  }

  @Override
  public void deleteBeerById(final UUID beerId) {
    beerRepository.deleteById(beerId);
  }
}
