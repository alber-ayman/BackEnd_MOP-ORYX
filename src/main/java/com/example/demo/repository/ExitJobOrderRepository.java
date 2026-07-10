package com.example.demo.repository;

import com.example.demo.models.ExitJobOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExitJobOrderRepository extends JpaRepository<ExitJobOrder, Long> {

    /*
     |--------------------------------------------------------------------------
     | BASIC FINDERS
     |--------------------------------------------------------------------------
     */

    List<ExitJobOrder> findByJobOrderId(String jobOrderId);

    List<ExitJobOrder> findByUnifiedSerial(String unifiedSerial);

    List<ExitJobOrder> findBySerialNumber(String serialNumber);

    List<ExitJobOrder> findByProjectProfileId(Long projectProfileId);

    Optional<ExitJobOrder> findByUniqueId(String uniqueId);

    void deleteByProjectProfileId(Long projectProfileId);

    void deleteByUnifiedSerial(String unifiedSerial);

    /*
     |--------------------------------------------------------------------------
     | SERIALS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT DISTINCT e.serialNumber
            FROM ExitJobOrder e
            WHERE e.jobOrderId = :jobOrderId
            ORDER BY e.serialNumber
            """)
    List<String> findDistinctSerialNumbersByJobOrderId(
            @Param("jobOrderId") String jobOrderId
    );

    @Query("""
            SELECT DISTINCT e.serialNumber
            FROM ExitJobOrder e
            WHERE e.projectProfileId = :projectProfileId
            ORDER BY e.serialNumber
            """)
    List<String> findDistinctSerialNumbersByProjectProfileId(
            @Param("projectProfileId") Long projectProfileId
    );

    @Query("""
            SELECT e.serialNumber
            FROM ExitJobOrder e
            ORDER BY e.id DESC
            LIMIT 1
            """)
    String findLastSerialNumber();

    /*
     |--------------------------------------------------------------------------
     | PANDS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT DISTINCT e.pandCode
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            AND e.serialNumber = :serialNumber
            ORDER BY e.pandCode
            """)
    List<String> findDistinctPandCodes(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId,
            @Param("serialNumber") String serialNumber
    );

    /*
     |--------------------------------------------------------------------------
     | UNITS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT e
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            AND e.unit = :unit
            AND e.serialNumber = :serialNumber
            ORDER BY e.thickness DESC
            """)
    List<ExitJobOrder> findByUnit(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId,
            @Param("unit") String unit,
            @Param("serialNumber") String serialNumber
    );

    @Query("""
            SELECT DISTINCT e.unit
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            AND e.serialNumber = :serialNumber
            """)
    List<String> findDistinctUnites(@Param("projectCode")String projectCode,
                                    @Param("jobOrderId")String jobOrderCode,
                                    @Param("serialNumber")String serial);


    /*
     |--------------------------------------------------------------------------
     | RAW TYPES
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT DISTINCT e.rawType
            FROM ExitJobOrder e
            WHERE e.projectProfileId = :projectProfileId
            ORDER BY e.rawType
            """)
    List<String> findDistinctRawTypes(
            @Param("projectProfileId") Long projectProfileId
    );

    @Query("""
            SELECT e
            FROM ExitJobOrder e
            WHERE e.projectProfileId = :projectProfileId
            AND e.rawType = :rawType
            AND e.returnFlag IS FALSE
            """)
    List<ExitJobOrder> findAllByRawType(
            @Param("projectProfileId") Long projectProfileId,
            @Param("rawType") String rawType
    );

    /*
     |--------------------------------------------------------------------------
     | RETURNS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT e
            FROM ExitJobOrder e
            WHERE e.jobOrderId = :jobOrderId
            AND e.returnFlag IS TRUE
            """)
    List<ExitJobOrder> findReturnedByJobOrderId(
            @Param("jobOrderId") String jobOrderId
    );

    /*
     |--------------------------------------------------------------------------
     | TOTALS & SUMS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT COALESCE(SUM(e.total), 0)
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.pandCode = :pandCode
            AND e.returnFlag IS FALSE
            """)
    Double sumTotalByPandCode(
            @Param("projectCode") String projectCode,
            @Param("pandCode") String pandCode
    );

    @Query("""
            SELECT COALESCE(SUM(e.total), 0)
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.pandCode = :pandCode
            AND e.rawType = :rawType
            AND e.thickness = :thickness
            AND e.returnFlag IS FALSE
            """)
    Double sumTotalByRawTypeAndThickness(
            @Param("projectCode") String projectCode,
            @Param("pandCode") String pandCode,
            @Param("rawType") String rawType,
            @Param("thickness") String thickness
    );

    @Query("""
            SELECT COALESCE(SUM(e.total), 0)
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            AND e.rawType = :rawType
            """)
    Double sumTotalByRawType(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT COALESCE(SUM(e.total), 0)
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.pandCode = :pandCode
            AND e.jobOrderId = :jobOrderId
            AND e.returnFlag IS FALSE
            """)
    Double sumTotalByJobOrderAndPand(
            @Param("projectCode") String projectCode,
            @Param("pandCode") String pandCode,
            @Param("jobOrderId") String jobOrderId
    );

    @Query("""
            SELECT COALESCE(SUM(e.quantity), 0)
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.rawType = :rawType
            AND e.returnFlag IS FALSE
            """)
    Double sumQuantityByRawType(
            @Param("projectCode") String projectCode,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT COALESCE(SUM(e.quantity), 0)
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            AND e.rawType = :rawType
            """)
    Double sumQuantityByJobOrderAndRawType(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT COALESCE(SUM(e.quantityUsedRaws), 0)
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.rawType = :rawType
            AND e.returnFlag IS FALSE
            """)
    Double sumQuantityUsedRaws(
            @Param("projectCode") String projectCode,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT COALESCE(SUM(e.quantity), 0)
            FROM ExitJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            """)
    Double sumQuantity(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId
    );
}