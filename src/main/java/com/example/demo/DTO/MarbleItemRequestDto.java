package com.example.demo.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MarbleItemRequestDto {
    private String projectName;
    private String projectCode;
    private Long projectProfileId;
    private String engineerName;
    private String floor;
    private String blockNumber;
    private String jobOrderType;
    private String installationArea;

    private List<MarbleItemDto> items;
}
