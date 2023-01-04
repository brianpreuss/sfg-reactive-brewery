package guru.springframework.sfgrestbrewery.web.controller;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;

@WebMvcTest(BeerController.class)
class BeerControllerTest {

  @MockBean
  BeerService beerService;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  BeerDto validBeer;

  @BeforeEach
  public void setUp() {
    validBeer = BeerDto.builder()
      .id(UUID.randomUUID())
      .beerName("Beer1")
      .beerStyle("PALE_ALE")
      .upc(BeerLoader.BEER_2_UPC)
      .build();
  }

  @Test
  void getBeer() throws Exception {
    given(beerService.getById(any(UUID.class), any())).willReturn(validBeer);

    mockMvc.perform(get("/api/v1/beer/" + validBeer.getId().toString()).accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.id", is(validBeer.getId().toString())))
      .andExpect(jsonPath("$.beerName", is("Beer1")));
  }

  @Test
  void handlePost() throws Exception {
    // given
    final var beerDto = validBeer;
    beerDto.setId(null);
    final var savedDto = BeerDto.builder().id(UUID.randomUUID()).beerName("New Beer").build();
    final var beerDtoJson = objectMapper.writeValueAsString(beerDto);

    given(beerService.saveNewBeer(any())).willReturn(savedDto);

    mockMvc.perform(post("/api/v1/beer").contentType(MediaType.APPLICATION_JSON).content(beerDtoJson))
      .andExpect(status().isCreated());
  }

  @Test
  void handleUpdate() throws Exception {
    // given
    final var beerDto = validBeer;
    beerDto.setId(null);
    final var beerDtoJson = objectMapper.writeValueAsString(beerDto);

    // when
    mockMvc
      .perform(put("/api/v1/beer/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(beerDtoJson))
      .andExpect(status().isNoContent());

    then(beerService).should().updateBeer(any(), any());
  }
}
