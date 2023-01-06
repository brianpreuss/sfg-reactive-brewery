package guru.springframework.sfgrestbrewery.web.controller;

import static guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig.BEER_V2_URL_ID;
import static guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig.BEER_V2_URL_UPC;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

/**
 * Created by jt on 4/11/21.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WebClientV2IT {
    public static final String BASE_URL = "http://localhost:8080";

    WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient
            .builder()
            .baseUrl(BASE_URL)
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
            .build();
    }

    @Test
    void getBeerById() throws InterruptedException {
        final var countDownLatch = new CountDownLatch(1);

        final Mono<BeerDto> beerDtoMono = webClient
            .get()
            .uri(BEER_V2_URL_ID, BeerLoader.BEER_1_UUID)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beer -> {
            assertThat(beer).isNotNull();
            assertThat(beer.getBeerName()).isNotNull();

            countDownLatch.countDown();
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isZero();
    }

    @Test
    void getBeerByIdNotFound() throws InterruptedException {
        final var countDownLatch = new CountDownLatch(1);

        final Mono<BeerDto> beerDtoMono = webClient
            .get()
            .uri(BEER_V2_URL_ID, UUID.randomUUID())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beer -> {

        }, throwable -> {
            countDownLatch.countDown();
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isZero();
    }

    @Test
    void getBeerByUpc() throws InterruptedException {
        final var countDownLatch = new CountDownLatch(1);

        final Mono<BeerDto> beerDtoMono = webClient
            .get()
            .uri(BEER_V2_URL_UPC, BeerLoader.BEER_1_UPC)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beer -> {
            assertThat(beer).isNotNull();
            assertThat(beer.getBeerName()).isNotNull();
            assertThat(beer.getUpc()).isEqualTo(BeerLoader.BEER_1_UPC);
            countDownLatch.countDown();
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isZero();
    }

    @Test
    void getBeerByUpcNotFound() throws InterruptedException {
        final var countDownLatch = new CountDownLatch(1);

        final Mono<BeerDto> beerDtoMono = webClient
            .get()
            .uri(BEER_V2_URL_UPC, "4711")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beer -> {

        }, throwable -> {
            countDownLatch.countDown();
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isZero();
    }

    @Test
    void testSaveBeer() throws InterruptedException {

        final var countDownLatch = new CountDownLatch(1);

        final var beerDto = BeerDto
            .builder()
            .beerName("JTs Beer")
            .upc("1233455")
            .beerStyle("PALE_ALE")
            .price(new BigDecimal("8.99"))
            .build();

        final var beerResponseMono = webClient
            .post()
            .uri(BeerRouterConfig.BEER_V2_URL)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(beerDto))
            .retrieve()
            .toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).subscribe(responseEntity -> {

            assertThat(responseEntity.getStatusCode().is2xxSuccessful());

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isZero();
    }

    @Test
    void testSaveBeerBadRequest() throws InterruptedException {

        final var countDownLatch = new CountDownLatch(1);

        final var beerDto = BeerDto
            .builder()
            .price(new BigDecimal("8.99"))
            .build();

        final var beerResponseMono = webClient
            .post()
            .uri(BeerRouterConfig.BEER_V2_URL)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(beerDto))
            .retrieve()
            .toBodilessEntity();

        beerResponseMono.subscribe(responseEntity -> {

        }, throwable -> {
            if ("org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest"
                .equals(throwable
                    .getClass()
                    .getName())) {
                final var ex = (WebClientResponseException) throwable;

                if (HttpStatus.BAD_REQUEST.equals(ex.getStatusCode())) {
                    countDownLatch.countDown();
                }
            }
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isZero();
    }

    @Test
    void testUpdateBeer() throws InterruptedException {
        final var newBeerName = "JTs Beer";
        final var beerId = BeerLoader.BEER_1_UUID;
        final var countDownLatch = new CountDownLatch(2);

        webClient
            .put()
            .uri(BeerRouterConfig.BEER_V2_URL_ID, beerId)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters
                .fromValue(BeerDto
                    .builder()
                    .beerName(newBeerName)
                    .upc("1233455")
                    .beerStyle("PALE_ALE")
                    .price(new BigDecimal("8.99"))
                    .build()))
            .retrieve()
            .toBodilessEntity()
            .subscribe(responseEntity -> {
                assertThat(responseEntity.getStatusCode().is2xxSuccessful());
                countDownLatch.countDown();
            });

        // wait for update thread to complete
        countDownLatch.await(500, TimeUnit.MILLISECONDS);

        webClient
            .get()
            .uri(BeerRouterConfig.BEER_V2_URL_ID, beerId)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(BeerDto.class)
            .subscribe(beer -> {
                assertThat(beer).isNotNull();
                assertThat(beer.getBeerName()).isNotNull();
                assertThat(beer.getBeerName()).isEqualTo(newBeerName);
                countDownLatch.countDown();
            });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isZero();
    }
}
