package com.example.demo.DTO;

import lombok.Data;

@Data
public class MarbleItemDto {

    private String thickness;
    private String height;
    private String width;
    private int quantity;
    private int repetition;
    private String unit;
    private String rawType;
    private String rawUsed;
    private String pandCode;
    private String description;
    private String additionalDescription;
    private double total;

}
