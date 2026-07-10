package com.example.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class MaterialIssueRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    private String projectName;
    private String projectNumber;
    private String workOrderNo;
    private String requestedBy;
    private LocalDate date;

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
