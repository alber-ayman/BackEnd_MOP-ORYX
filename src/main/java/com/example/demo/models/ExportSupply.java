package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ExportSupply")
public class ExportSupply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    private String data;

    private String time;

    private String workOrder;

    private String storeOfficer;

    private String projectCode;

    @Column(unique = true)
    private String supplyNumber;

}
