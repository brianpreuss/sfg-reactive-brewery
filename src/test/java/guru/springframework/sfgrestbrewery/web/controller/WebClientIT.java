package guru.springframework.sfgrestbrewery.web.controller;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

/**
 * Created by jt on 3/7/21.
 */
@Disabled
public class WebClientIT {

  public static final String BASE_URL = "http://localhost:8080";

  WebClient webClient;

  @BeforeEach
  void setUp() {
    webClient = WebClient.builder()
      .baseUrl(BASE_URL)
      .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
      .build();
  }

  @Test
  void testListBeers() throws InterruptedException {

    final var countDownLatch = new CountDownLatch(1);

    final Mono<BeerPagedList> beerPagedListMono = webClient.get()
      .uri("/api/v1/beer")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(BeerPagedList.class);

    // BeerPagedList pagedList = beerPagedListMono.block();
    // pagedList.getContent().forEach(beerDto ->
    // System.out.println(beerDto.toString()));
    beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

      beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

      countDownLatch.countDown();
    });

    countDownLatch.await();
  }
}
