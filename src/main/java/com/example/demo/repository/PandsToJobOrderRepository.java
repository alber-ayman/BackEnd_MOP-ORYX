package com.example.demo.repository;

import com.example.demo.DTO.ThicknessUnitDTO;
import com.example.demo.models.PandsToJobOrder;
import com.example.demo.payload.RawTypeDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PandsToJobOrderRepository extends JpaRepository<PandsToJobOrder, Long> {

    /*
     |--------------------------------------------------------------------------
     | BASIC FINDERS
     |--------------------------------------------------------------------------
     */

    List<PandsToJobOrder> findByJobOrderId(String jobOrderId);

    List<PandsToJobOrder> findByProjectProfileId(Long projectProfileId);

    List<PandsToJobOrder> findByPandCodeAndProjectCode(String pandCode, String projectCode);

    Optional<PandsToJobOrder> findByUniqueId(String uniqueId);

    Optional<PandsToJobOrder> findByJobOrderIdAndPandCode(
            String jobOrderId,
            String pandCode
    );

    Optional<PandsToJobOrder> findByUniqueIdAndJobOrderIdAndWidthAndHeight(
            String uniqueId,
            String jobOrderId,
            String width,
            String height
    );

    /*
     |--------------------------------------------------------------------------
     | UNITS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT DISTINCT p.unit
            FROM PandsToJobOrder p
            WHERE p.jobOrderId = :jobOrderId
            ORDER BY p.unit
            """)
    List<String> findDistinctUnitsByJobOrderId(
            @Param("jobOrderId") String jobOrderId
    );

    @Query("""
            SELECT p
            FROM PandsToJobOrder p
            WHERE p.unit = :unit
            AND p.jobOrderId = :jobOrderId
            """)
    List<PandsToJobOrder> findByUnitAndJobOrderId(
            @Param("unit") String unit,
            @Param("jobOrderId") String jobOrderId
    );

    /*
     |--------------------------------------------------------------------------
     | JOB ORDERS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT DISTINCT p.jobOrderId
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            """)
    List<String> findDistinctJobOrdersByProjectProfileId(
            @Param("projectProfileId") Long projectProfileId
    );

    @Query("""
            SELECT p
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.jobOrderId = :jobOrderId
            """)
    List<PandsToJobOrder> findByProjectProfileIdAndJobOrderId(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId
    );

    @Query("""
            SELECT DISTINCT p.pandCode
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.jobOrderId = :jobOrderId
            """)
    List<String> findDistinctPandCodesByProjectProfileIdAndJobOrderId(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId
    );

    /*
     |--------------------------------------------------------------------------
     | PANDS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT p
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.pandCode = :pandCode
            """)
    List<PandsToJobOrder> findByProjectProfileIdAndPandCode(
            @Param("projectProfileId") Long projectProfileId,
            @Param("pandCode") String pandCode
    );

    @Query("""
            SELECT DISTINCT p.jobOrderId
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.pandCode = :pandCode
            """)
    List<String> findPandDetails(
            @Param("projectProfileId") Long projectProfileId,
            @Param("pandCode") String pandCode
    );

    /*
     |--------------------------------------------------------------------------
     | RAW TYPES
     |--------------------------------------------------------------------------
     */


    @Query("""
            SELECT DISTINCT p.rawType
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.jobOrderId = :jobOrderId
            """)
    List<String> findDistinctRawTypesByProjectAndJobOrder(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId
    );

    @Query("""
            SELECT p
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.jobOrderId = :jobOrderId
            AND p.rawType = :rawType
            """)
    List<PandsToJobOrder> findByProjectProfileIdAndJobOrderIdAndRawType(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT DISTINCT p
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.jobOrderId = :jobOrderId
            ORDER BY p.rawType
            """)
    List<PandsToJobOrder> findGroupedByRawType(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId
    );

    @Query("""
            SELECT DISTINCT p
            FROM PandsToJobOrder p
            WHERE p.jobOrderId = :jobOrderId
            ORDER BY p.rawType
            """)
    List<PandsToJobOrder> allJobOrderIdGroupByRawType(
            @Param("jobOrderId") String jobOrderId
    );

    /*
     |--------------------------------------------------------------------------
     | THICKNESS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT DISTINCT p.thickness
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectCode
            AND p.jobOrderId = :jobOrderId
            AND p.rawType = :rawType
            ORDER BY p.thickness ASC
            """)
    List<String> findDistinctThicknesses(
            @Param("projectCode") Long projectCode,
            @Param("jobOrderId") String jobOrderId,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT p
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.jobOrderId = :jobOrderId
            AND p.rawType = :rawType
            AND p.thickness = :thickness
            AND p.unit = :unit
            """)
    List<PandsToJobOrder> findByThicknessAndRawTypeAndUnit(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId,
            @Param("rawType") String rawType,
            @Param("thickness") String thickness,
            @Param("unit") String unit
    );

    @Query(value = """
            SELECT DISTINCT
                   thickness AS thickness,
                   unit AS unit,
                   raw_type As rawType
            FROM pands_to_job_order
            WHERE project_profile_id = :projectCode
              AND job_order_id = :jobOrderId
              AND raw_type = :rawType
            ORDER BY thickness ASC
            """, nativeQuery = true)
    List<ThicknessUnitDTO> findDistinctThicknessAndUnit(
            @Param("projectCode") Long projectCode,
            @Param("jobOrderId") String jobOrderId,
            @Param("rawType") String rawType
    );

    /*
     |--------------------------------------------------------------------------
     | SUMS & TOTALS
     |--------------------------------------------------------------------------
     */

    @Query("""
            SELECT COALESCE(SUM(p.mainTotal), 0)
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.pandCode = :pandCode
            """)
    Double sumMainTotalByPandCode(
            @Param("projectProfileId") Long projectProfileId,
            @Param("pandCode") String pandCode
    );

    @Query("""
            SELECT COALESCE(SUM(p.mainTotal), 0)
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.pandCode = :pandCode
            AND p.rawType = :rawType
            AND p.thickness = :thickness
            """)
    Double sumMainTotalByRawTypeAndThickness(
            @Param("projectProfileId") Long projectProfileId,
            @Param("pandCode") String pandCode,
            @Param("rawType") String rawType,
            @Param("thickness") String thickness
    );

    @Query("""
            SELECT COALESCE(SUM(p.mainTotal), 0)
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.jobOrderId = :jobOrderId
            AND p.rawType = :rawType
            """)
    Double sumMainTotalByRawType(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT COALESCE(SUM(p.mainQuantity), 0)
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.jobOrderId = :jobOrderId
            AND p.rawType = :rawType
            """)
    Double sumMainQuantityByRawType(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.rawType = :rawType
            """)
    Double sumQuantityByRawType(
            @Param("projectProfileId") Long projectProfileId,
            @Param("rawType") String rawType
    );

    @Query("""
            SELECT COALESCE(SUM(p.mainTotal), 0)
            FROM PandsToJobOrder p
            WHERE p.projectProfileId = :projectProfileId
            AND p.pandCode = :pandCode
            AND p.jobOrderId = :jobOrderId
            """)
    Double sumMainTotalByPandCodeAndJobOrder(
            @Param("projectProfileId") Long projectProfileId,
            @Param("pandCode") String pandCode,
            @Param("jobOrderId") String jobOrderId
    );

    /*
     |--------------------------------------------------------------------------
     | REPORTS
     |--------------------------------------------------------------------------
     */

    @Query(value = """
            SELECT
                raw_type AS rawType,
                SUM(main_total) AS total
            FROM pands_to_job_order
            WHERE project_profile_id = :projectProfileId
            AND job_order_id = :jobOrderId
            GROUP BY raw_type
            """, nativeQuery = true)
    List<RawTypeDTO> getMaterialsByWorkOrder(
            @Param("projectProfileId") Long projectProfileId,
            @Param("jobOrderId") String jobOrderId
    );

    /*
     |--------------------------------------------------------------------------
     | DELETES
     |--------------------------------------------------------------------------
     */

    @Transactional
    @Modifying
    @Query(value = """
            DELETE FROM job_order_pands_to_job_orders
            WHERE pands_to_job_orders_id = :id
            """, nativeQuery = true)
    void deleteRelationByPandsToJobOrderId(@Param("id") Long id);

    void deleteByProjectProfileId(Long projectProfileId);

    /*
     |--------------------------------------------------------------------------
     | PROCEDURES
     |--------------------------------------------------------------------------
     */

    @Procedure(procedureName = "deducting_quantity_from_pands")
    void deductingQuantityFromPands(
            @Param("p_project_code") String projectCode,
            @Param("p_job_order_id") String jobOrderId
    );

//    @Procedure(procedureName = "UpdatePandsToJobOrder")
//    void updatePandsToJobOrder(
//            Long p_id,
//            String p_projectCode,
//            String p_projectName,
//            String p_engineerName,
//            String p_jobOrderType,
//            String p_manufacturingCode,
//            String p_pandCode,
//            String p_description,
//            String p_manufacturing,
//            String p_rawType,
//            String p_rawUsed,
//            String p_finishType,
//            String p_thickness,
//            String p_blockNumber,
//            String p_floor,
//            String p_unit,
//            String p_additionalDescription,
//            Double p_height,
//            Double p_width,
//            Double p_repetition,
//            Double p_mainQuantity,
//            String p_installationArea
//    );
}