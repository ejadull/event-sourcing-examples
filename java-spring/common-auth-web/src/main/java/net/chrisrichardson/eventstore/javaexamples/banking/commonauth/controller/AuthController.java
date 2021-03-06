package net.chrisrichardson.eventstore.javaexamples.banking.commonauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.eventstore.javaexamples.banking.common.customers.QuerySideCustomer;
import net.chrisrichardson.eventstore.javaexamples.banking.commonauth.CustomerAuthService;
import net.chrisrichardson.eventstore.javaexamples.banking.commonauth.model.AuthRequest;
import net.chrisrichardson.eventstore.javaexamples.banking.commonauth.model.ErrorResponse;
import net.chrisrichardson.eventstore.javaexamples.banking.commonauth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by popikyardo on 21.09.15.
 */
@RestController
@Validated
@RequestMapping("/api")
public class AuthController {

  @Autowired
  private TokenService tokenService;

  @Autowired
  private CustomerAuthService customerAuthService;

  private static ObjectMapper objectMapper = new ObjectMapper();

  @RequestMapping(value = "/login", method = POST)
  public ResponseEntity<QuerySideCustomer> doAuth(@RequestBody @Valid AuthRequest request) throws IOException {
    QuerySideCustomer customer = customerAuthService.findByEmail(request.getEmail());

    Token token = tokenService.allocateToken(objectMapper.writeValueAsString(new User(request.getEmail())));
    return ResponseEntity.status(HttpStatus.OK).header("access-token", token.getKey())
            .body(customer);
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
  public ErrorResponse customersNotFound() {
    return new ErrorResponse("Customer not found");
  }

  @RequestMapping(value = "/user", method = GET)
  public ResponseEntity<QuerySideCustomer> getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    return ResponseEntity.status(HttpStatus.OK).body(customerAuthService.findByEmail(auth.getName()));
  }


}
