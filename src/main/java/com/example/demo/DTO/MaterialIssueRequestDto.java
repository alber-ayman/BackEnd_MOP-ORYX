package com.example.demo.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MaterialIssueRequestDto {
    private String projectName;
    private String projectNumber;
    private String workOrderNo;
    private String requestedBy;
    private LocalDate date;

    private List<MaterialIssueItemDto> items;
}
