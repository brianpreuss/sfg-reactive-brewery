package guru.springframework.sfgrestbrewery.web.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import guru.springframework.sfgrestbrewery.services.CustomerService;
import guru.springframework.sfgrestbrewery.web.model.CustomerDto;

/**
 * Created by jt on 2019-04-21.
 */

@RequestMapping("api/v1/customer")
@RestController
public class CustomerController {

  private final CustomerService customerService;

  public CustomerController(final CustomerService customerService) {
    this.customerService = customerService;
  }

  @GetMapping("/{customerId}")
  public ResponseEntity<CustomerDto> getCustomer(@PathVariable("customerId") final UUID customerId) {

    return new ResponseEntity<>(customerService.getCustomerById(customerId), HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<Void> handlePost(@RequestBody @Validated final CustomerDto customerDto) {
    final var savedDto = customerService.saveNewCustomer(customerDto);

    final var httpHeaders = new HttpHeaders();
    httpHeaders.add("Location", "/api/v1/customer/" + savedDto.getId().toString());

    return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
  }

  @PutMapping("/{customerId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void handleUpdate(
      @PathVariable("customerId") final UUID customerId,
      @Validated @RequestBody final CustomerDto customerDto) {
    customerService.updateCustomer(customerId, customerDto);
  }

  @DeleteMapping("/{customerId}")
  public void deleteById(@PathVariable("customerId") final UUID customerId) {
    customerService.deleteById(customerId);
  }

}
