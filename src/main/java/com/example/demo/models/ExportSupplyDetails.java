package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ExportSupplyDetails")
public class ExportSupplyDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    private String exportSupplierCode;

    private String supplierCode;

    private String supplierName;

    private String storeOfficer;

    private String supplyNumber;

    private String price;

    private String material;

    private String category;

    private String shape;

    private String unit;

    private String thickness;

    private String finishing;

    private String number;

    private String height;

    private String width;

    private String total;

    private String cost;

    private String supplyCode;

    private String data;

    private String time;

    private int responseFlag;

    private String responseMessage;

    private String projectCode;

    private String workOrder;

}
