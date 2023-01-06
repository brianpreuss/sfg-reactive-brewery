package guru.springframework.sfgrestbrewery.web.functional;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BeerHandlerV2 {
    private final BeerService beerService;

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
        final Mono<BeerDto> beerDtoMono = request.bodyToMono(BeerDto.class);

        return beerService
            .saveNewBeerMono(beerDtoMono)
            .flatMap(beerDto -> ServerResponse
                .created(UriComponentsBuilder.fromPath(BeerRouterConfig.BEER_V2_URL_ID).build(beerDto.getId()))
                .build());
    }
}
