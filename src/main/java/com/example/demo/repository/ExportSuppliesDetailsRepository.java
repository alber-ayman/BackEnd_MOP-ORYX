package com.example.demo.repository;

import com.example.demo.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportSuppliesDetailsRepository extends JpaRepository<ExportSupplyDetails,Long> {
    ExportSupplyDetails  findAllById(Long id);

    List<ExportSupplyDetails> getAllBySupplierCode(String supplierCode);

    List<ExportSupplyDetails> getAllBySupplyNumber(String supplierNumber);

    @Query(value = "SELECT DISTINCT category, shape,thickness,finishing FROM export_supply_details where supplier_code = :supplierCode and material = :material", nativeQuery = true)
    List<SupplyDetailsProjection> getSuppliesDetailsByMaterial(@Param("supplierCode") String supplierCode, @Param("material") String material);

    List<ExportSupplyDetails> getAllByMaterial(String material);

    ExportSupplyDetails getAllBySupplyCode(String id);

    @Query(value = "SELECT  sum(number) as number,sum(total) as total FROM export_supply_details where supply_code = :supplyCode" , nativeQuery = true)
    BalanceReport getDeductedNumberBySupplyCode(@Param("supplyCode") String supplyCode);

    @Query(value = """
        SELECT sum(number) as number,sum(total) as total FROM export_supply_details 
        WHERE (:project IS NULL OR project_code = :project)
        AND (:jobOrder IS NULL OR work_order = :jobOrder)
        AND (:material IS NULL OR material = :material)
        """, nativeQuery = true)
    BalanceReport materialFilter(
            @Param("project") String project,
            @Param("jobOrder") String jobOrder,
            @Param("material") String material
    );

    @Query(value = """
        SELECT DISTINCT material FROM export_supply_details 
        WHERE project_code = :project
        AND (:jobOrder IS NULL OR work_order = :jobOrder)
        """, nativeQuery = true)
    List<String> getMaterialsByProjectCode(@Param("project") String project,
                                           @Param("jobOrder") String jobOrder);

    @Query(value = "SELECT  sum(number) as number,sum(total) as total FROM export_supply_details where project_code = :projectCode and material = :material" , nativeQuery = true)
    BalanceReport getDeductedNumberByProjectAndMaterial(@Param("projectCode")String project,@Param("material") String material);

    @Query(value = "SELECT DISTINCT material FROM export_supply_details where project_code = :projectCode", nativeQuery = true)
    List<String> getMaterialByProjectCode(@Param("projectCode")String project);

    @Query(value = "SELECT DISTINCT material FROM export_supply_details where project_code = :projectCode and work_order = :workOrder", nativeQuery = true)
    List<String> getMaterialByProjectCodeAndWorkOrder(@Param("projectCode")String projectCode,@Param("workOrder") String workOrder);

    @Query(value = "SELECT DISTINCT work_order FROM export_supply_details where project_code = :projectCode", nativeQuery = true)
    List<String> getWorkOrderByProjectCode(@Param("projectCode")String projectCode);

    @Query(value = "SELECT DISTINCT project_code FROM export_supply_details", nativeQuery = true)
    List<String> getExportedProjects();
}
