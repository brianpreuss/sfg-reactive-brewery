package guru.springframework.sfgrestbrewery.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;

/**
 * Created by jt on 2019-05-25.
 */
@Mapper(uses = { DateMapper.class })
public interface BeerMapper {
  @Mapping(target = "lastUpdatedDate", ignore = true)
  @Mapping(target = "quantityOnHand", ignore = true)
  BeerDto beerToBeerDto(Beer beer);

  @Mapping(target = "lastUpdatedDate", ignore = true)
  BeerDto beerToBeerDtoWithInventory(Beer beer);

  @Mapping(target = "lastModifiedDate", ignore = true)
  @Mapping(target = "version", ignore = true)
  Beer beerDtoToBeer(BeerDto dto);
}
