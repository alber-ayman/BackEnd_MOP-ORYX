package com.example.demo.payload;

import lombok.Data;

@Data
public class FilterResponse {
    private String material;
    private String code;
    private String number;
    private String price;
    private String total;
    private String cost;
}
