package guru.springframework.sfgrestbrewery.web.functional;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import guru.springframework.sfgrestbrewery.services.BeerService;
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
}
