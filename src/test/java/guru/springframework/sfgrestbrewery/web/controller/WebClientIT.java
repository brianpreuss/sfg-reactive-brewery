package guru.springframework.sfgrestbrewery.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import io.netty.handler.logging.LogLevel;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

/**
 * Created by jt on 3/7/21.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WebClientIT {

  public static final String BASE_URL = "http://localhost:8080";

  WebClient webClient;

  @BeforeEach
  void setUp() {
    webClient = WebClient.builder()
      .baseUrl(BASE_URL)
      .clientConnector(
        new ReactorClientHttpConnector(
          HttpClient.create().wiretap("reactor.netty.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
        )
      )
      .build();
  }

  @Test
  void getBeerById() throws InterruptedException {
    final var countDownLatch = new CountDownLatch(1);

    final Mono<BeerDto> beerDtoMono = webClient.get()
      .uri("api/v1/beer/{id}", BeerLoader.BEER_1_UUID)
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(BeerDto.class);

    beerDtoMono.subscribe(beer -> {
      assertThat(beer).isNotNull();
      assertThat(beer.getBeerName()).isNotNull();

      countDownLatch.countDown();
    });

    countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    assertThat(countDownLatch.getCount()).isZero();
  }

  @Test
  void getBeerByUpc() throws InterruptedException {
    final var countDownLatch = new CountDownLatch(1);

    final Mono<BeerDto> beerDtoMono = webClient.get()
      .uri("api/v1/beerUpc/{upc}", BeerLoader.BEER_1_UPC)
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(BeerDto.class);

    beerDtoMono.subscribe(beer -> {
      assertThat(beer).isNotNull();
      assertThat(beer.getBeerName()).isNotNull();
      assertThat(beer.getUpc()).isEqualTo(BeerLoader.BEER_1_UPC);

      countDownLatch.countDown();
    });

    countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    assertThat(countDownLatch.getCount()).isZero();
  }

  @Test
  void testListBeers() throws InterruptedException {

    final var countDownLatch = new CountDownLatch(1);

    final Mono<BeerPagedList> beerPagedListMono = webClient.get()
      .uri("/api/v1/beer")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(BeerPagedList.class);

    beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

      beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

      countDownLatch.countDown();
    });

    countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    assertThat(countDownLatch.getCount()).isZero();
  }

  @Test
  void testListBeersPageSize5() throws InterruptedException {

    final var countDownLatch = new CountDownLatch(1);

    final Mono<BeerPagedList> beerPagedListMono = webClient.get()
      .uri(uriBuilder -> uriBuilder.path("/api/v1/beer").queryParam("pageSize", "5").build())
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(BeerPagedList.class);

    beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

      beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

      countDownLatch.countDown();
    });

    countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    assertThat(countDownLatch.getCount()).isZero();
  }

  @Test
  void testListBeersByName() throws InterruptedException {

    final var countDownLatch = new CountDownLatch(1);

    final Mono<BeerPagedList> beerPagedListMono = webClient.get()
      .uri(uriBuilder -> uriBuilder.path("/api/v1/beer").queryParam("beerName", "Mango Bobs").build())
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(BeerPagedList.class);

    beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

      beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

      countDownLatch.countDown();
    });

    countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    assertThat(countDownLatch.getCount()).isZero();
  }

  @Test
  void testSaveBeer() throws InterruptedException {

    final var countDownLatch = new CountDownLatch(1);

    final var beerDto = BeerDto.builder()
      .beerName("JTs Beer")
      .upc("1233455")
      .beerStyle("PALE_ALE")
      .price(new BigDecimal("8.99"))
      .build();

    final var beerResponseMono = webClient.post()
      .uri("/api/v1/beer")
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

    final var beerDto = BeerDto.builder().price(new BigDecimal("8.99")).build();

    final var beerResponseMono = webClient.post()
      .uri("/api/v1/beer")
      .accept(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(beerDto))
      .retrieve()
      .toBodilessEntity();

    beerResponseMono.publishOn(Schedulers.parallel()).doOnError(throwable -> {
      countDownLatch.countDown();
    }).subscribe(responseEntity -> {

    });

    countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    assertThat(countDownLatch.getCount()).isZero();
  }

  @Test
  void testUpdateBeer() throws InterruptedException {

    final var countDownLatch = new CountDownLatch(3);

    webClient.get()
      .uri("/api/v1/beer")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(BeerPagedList.class)
      .publishOn(Schedulers.single())
      .subscribe(pagedList -> {
        countDownLatch.countDown();

        // get existing beer
        final var beerDto = pagedList.getContent().get(0);

        final var updatePayload = BeerDto.builder()
          .beerName("JTsUpdate")
          .beerStyle(beerDto.getBeerStyle())
          .upc(beerDto.getUpc())
          .price(beerDto.getPrice())
          .build();

        // update existing beer
        webClient.put()
          .uri("/api/v1/beer/" + beerDto.getId())
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(updatePayload))
          .retrieve()
          .toBodilessEntity()
          .flatMap(responseEntity -> {
            // get and verify update
            countDownLatch.countDown();
            return webClient.get()
              .uri("/api/v1/beer/" + beerDto.getId())
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .bodyToMono(BeerDto.class);
          })
          .subscribe(savedDto -> {
            assertThat(savedDto.getBeerName()).isEqualTo("JTsUpdate");
            countDownLatch.countDown();
          });
      });

    countDownLatch.await(1000, TimeUnit.MILLISECONDS);
    assertThat(countDownLatch.getCount()).isZero();
  }
}
