package com.example.demo.repository;

import com.example.demo.models.ExitProcessJobOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitProcessJobOrderRepository
        extends JpaRepository<ExitProcessJobOrder, Long> {

    /*
     |--------------------------------------------------------------------------
     | UNITS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT DISTINCT e.unit
            FROM ExitProcessJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            ORDER BY e.unit
            """)
    List<String> findDistinctUnitsByProjectCodeAndJobOrderId(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId
    );

    @Query("""
            SELECT e
            FROM ExitProcessJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            AND e.unit = :unit
            ORDER BY e.thickness DESC
            """)
    List<ExitProcessJobOrder> findByProjectCodeAndJobOrderIdAndUnit(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId,
            @Param("unit") String unit
    );

    /*
     |--------------------------------------------------------------------------
     | PANDS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT DISTINCT e.pandCode
            FROM ExitProcessJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            ORDER BY e.pandCode
            """)
    List<String> findDistinctPandCodesByProjectCodeAndJobOrderId(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId
    );

    /*
     |--------------------------------------------------------------------------
     | TOTALS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT COALESCE(SUM(e.total), 0)
            FROM ExitProcessJobOrder e
            WHERE e.projectCode = :projectCode
            AND e.jobOrderId = :jobOrderId
            AND e.pandCode = :pandCode
            """)
    Double sumTotalForBill(
            @Param("projectCode") String projectCode,
            @Param("jobOrderId") String jobOrderId,
            @Param("pandCode") String pandCode
    );
}