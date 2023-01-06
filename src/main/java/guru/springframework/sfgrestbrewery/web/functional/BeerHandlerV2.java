package guru.springframework.sfgrestbrewery.web.functional;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerHandlerV2 {
    private final BeerService beerService;
    private final Validator validator;

    public Mono<ServerResponse> getBeerById(final ServerRequest request) {
        final var beerId = UUID.fromString(request.pathVariable("beerId"));
        final var showInventory = Boolean.parseBoolean(request.queryParam("showInventory").orElse("false"));

        return beerService
            .getById(beerId, showInventory)
            .flatMap(beerDto -> ServerResponse
                .ok()
                .bodyValue(beerDto))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getBeerByUpc(final ServerRequest request) {
        final var upc = request.pathVariable("upc");

        return beerService
            .getByUpc(upc)
            .flatMap(beerDto -> ServerResponse.ok().bodyValue(beerDto))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> saveNewBeer(final ServerRequest request) {
        final var beerDtoMono = request.bodyToMono(BeerDto.class).doOnNext(this::validate);

        return beerService
            .saveNewBeerMono(beerDtoMono)
            .flatMap(beerDto -> ServerResponse
                .created(UriComponentsBuilder.fromPath(BeerRouterConfig.BEER_V2_URL_ID).build(beerDto.getId()))
                .build());
    }

    private void validate(final BeerDto beerDto) {
        final var errors = new BeanPropertyBindingResult(beerDto, "beerDto");
        validator.validate(beerDto, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

    public Mono<ServerResponse> updateBeer(final ServerRequest request) {
        return request
            .bodyToMono(BeerDto.class)
            .doOnNext(this::validate)
            .flatMap(beerDto -> beerService.updateBeer(UUID.fromString(request.pathVariable("beerId")), beerDto))
            .doOnNext(savedBeerDto -> log.debug("Saved Beer Id: {}", savedBeerDto.getId()))
            .flatMap(savedBeerDto -> ServerResponse.noContent().build());
    }
}
