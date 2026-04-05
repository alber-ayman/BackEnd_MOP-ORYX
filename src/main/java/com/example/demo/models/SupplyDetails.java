package com.example.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import jdk.jfr.Unsigned;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "SupplyDetails")
public class SupplyDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

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

    @PositiveOrZero
    @Unsigned
    private String restNumber;

    private String height;

    private String width;

    private String total;

    @PositiveOrZero
    @Unsigned
    private String restTotal;

    private String cost;

    private String supplyCode;

    private String data;

    private String time;

    private int responseFlag;

    private String responseMessage;

}
