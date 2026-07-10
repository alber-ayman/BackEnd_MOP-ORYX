package com.example.demo.payload.login.response;

import com.example.demo.models.User;
import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
  private String token;
  private String type = "Bearer";
  private Long id;
  private String username;
  private String email;
  private final List<String> roles;

  private User user;

  public JwtResponse(String accessToken, Long id, String username, String email, List<String> roles, User user) {
    this.token = accessToken;
    this.id = id;
    this.username = username;
    this.email = email;
    this.roles = roles;
    this.user = user;
  }

}
