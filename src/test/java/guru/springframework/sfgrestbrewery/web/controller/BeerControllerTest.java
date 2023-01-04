package guru.springframework.sfgrestbrewery.web.controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import lombok.val;

@WebFluxTest(BeerController.class)
class BeerControllerTest {
  @Autowired
  WebTestClient webTestClient;

  @MockBean
  BeerService beerService;

  BeerDto validBeer;

  @BeforeEach
  void setUp() throws Exception {
    validBeer = BeerDto.builder().beerName("Test beer").beerStyle("PALE ALE").upc(BeerLoader.BEER_1_UPC).build();
  }

  @Test

  void testGetBeerById() {
    final val beerId = UUID.randomUUID();
    given(beerService.getById(any(), any())).willReturn(validBeer);
    webTestClient.get()
      .uri("/api/v1/beer/{beerId}", beerId)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(BeerDto.class)
      .value(BeerDto::getBeerName, equalTo(validBeer.getBeerName()));
  }

  @Test
  void testListBeers() {
    final val mockedBeerPagedList = new BeerPagedList(List.of(validBeer));
    given(beerService.listBeers(any(), any(), any(), any())).willReturn(mockedBeerPagedList);
    webTestClient.get()
      .uri("/api/v1/beer")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(BeerPagedList.class)
      .value(beerPagedList -> beerPagedList.getContent().get(0).getBeerName(), equalTo(validBeer.getBeerName()));
  }

  @Test
  void testGetBeerByUpc() {
    given(beerService.getByUpc(validBeer.getUpc())).willReturn(validBeer);
    webTestClient.get()
      .uri("/api/v1/beerUpc/{upc}", validBeer.getUpc())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(BeerDto.class)
      .value(BeerDto::getBeerName, equalTo(validBeer.getBeerName()));
  }

  @Test
  @Disabled
  void testSaveNewBeer() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  void testUpdateBeerById() {
    fail("Not yet implemented");
  }

  @Test
  @Disabled
  void testDeleteBeerById() {
    fail("Not yet implemented");
  }
}
