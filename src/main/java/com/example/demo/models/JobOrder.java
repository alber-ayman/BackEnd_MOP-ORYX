package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "Job_Order")
public class JobOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @SequenceGenerator(sequenceName = "PAND_SEQ", allocationSize = 1, name = "PAND_SEQ")
    @Column(name = "ID")
    private Long id;

    private String jobOrderNumber;

    private String jobOrderName;

    private int incNumber;

    private String projectName;

    private Integer  number;

    private String projectCode;

    private String jobOrderDate;

    private int year;

    private String jobOrderTime;

    private Long projectProfileId;

    private String fileDB;

    private String fileId;

    private String createdBy;

    private String installementArea;

    private boolean approved;

    private boolean manfacturingApprove;

    private boolean storeApproved;

    private boolean purchaseApproved;

    private boolean reverted;

    private boolean manufacturingManager;

    private boolean storeManager;

    private boolean purchasingManager;

    private boolean generalManager;

    private String sendingNote;

    private boolean commit;

    private String pendingFor;

    private String workOrderHeader;

    private Double totalDelivered;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PandsToJobOrder> pandsToJobOrders;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    private String imageDescription;

    private String engineerName;

}
