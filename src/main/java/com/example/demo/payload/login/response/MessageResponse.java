package com.example.demo.payload.login.response;

import lombok.Getter;
import lombok.Setter;

public class MessageResponse {
  @Setter
  @Getter
  private String message;

  private int flag;

  public MessageResponse(String message, int flag) {
    this.message = message;
  }

}
