package guru.springframework.sfgrestbrewery.web.functional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class BeerRouterConfig {
    public static final String API_V2_URL = "/api/v2";
    public static final String BEER_V2_URL = API_V2_URL + "/beer";
    public static final String BEER_V2_URL_ID = BEER_V2_URL + "/{beerId}";
    public static final String BEER_V2_URL_UPC = API_V2_URL + "/beerUpc/{upc}";

    @Bean
    RouterFunction<ServerResponse> beerRoutesV2(final BeerHandlerV2 beerHandler) {
        return route()
            .GET(BEER_V2_URL_ID, accept(APPLICATION_JSON), beerHandler::getBeerById)
            .GET(BEER_V2_URL_UPC, accept(APPLICATION_JSON), beerHandler::getBeerByUpc)
            .POST(BEER_V2_URL, accept(APPLICATION_JSON), beerHandler::saveNewBeer)
            .PUT(BEER_V2_URL_ID, accept(APPLICATION_JSON), beerHandler::updateBeer)
            .DELETE(BEER_V2_URL_ID, accept(APPLICATION_JSON), beerHandler::deleteBeer)
            .build();
    }
}
