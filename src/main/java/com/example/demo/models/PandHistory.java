package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PandHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    private String pandCode;

    private Long pandId;

    private String additionalQuantityDate ;

    private String additionalReason ;

    private String additionalBy ;

    private double additionalQuantity ;

    private double totalAfterAddition;

}
