package com.example.demo.payload.excel.message;

import lombok.Data;

@Data
public class ResponseMessage {
  private String message;
  private String fileId;

  public ResponseMessage(String message,String fileId) {
    this.message = message;
    this.fileId = fileId;
  }
}
