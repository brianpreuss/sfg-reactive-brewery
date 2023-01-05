package guru.springframework.sfgrestbrewery.web.controller;

import static guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig.BEER_V2_URL_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import reactor.core.publisher.Mono;
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
}
