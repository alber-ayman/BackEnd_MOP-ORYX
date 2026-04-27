package com.example.demo.models;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "PAND")
public class Pand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @SequenceGenerator(sequenceName = "PAND_SEQ", allocationSize = 1, name = "PAND_SEQ")
    @Column(name = "ID")
    private Long id;

    private String pandCode;

    private String projectCode;

    private String projectName;

    private String engineerName;

    private Long projectProfileId;

    private String description;

    private String additionalDescription;

    private double restQuantity;

    private double mainQuantity;

    private double mockQuantity;

    private String unit;

    private String updatedDate;

    private String totalPrice;

    private String rawType;

    private String rawUsed;

    private String finishType;

    private String thickness;

    private String height;

    private String width;

    private String repetition;

    private double total;

    private String fileDB;

    private String fileId;

    private int flag;

    private String message;

    private double totalQuantity ;

    private double price ;

    private String imageDescription;

    private double rawMainQuantity;

    private String manufacturing;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

}
