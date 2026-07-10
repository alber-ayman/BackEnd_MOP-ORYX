package com.example.demo.DTO;

import lombok.Data;

@Data
public class MaterialIssueItemDto {

    private Integer no;

    private String item;

    private Double thickCm;

    private String bundleNo;

    private Integer noOfSlabs;

    private Double  l;

    private Double  w;

    private Double  totalSqm;

    private String issueReturn;

}
