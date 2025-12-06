package com.example.mqlabs.lab01;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
  private final UserService service;
  public UserController(UserService service) { this.service = service; }
  @PostMapping("/lab01/users/{id}/status")
  public String change(@PathVariable("id") String id, @RequestParam("value") String value) {
    service.changeStatusAndOutbox(id, value);
    return "OK";
  }
}
