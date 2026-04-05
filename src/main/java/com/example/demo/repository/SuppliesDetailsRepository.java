package com.example.demo.repository;

import com.example.demo.models.BalanceReport;
import com.example.demo.models.SupplyDetails;
import com.example.demo.models.SupplyDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuppliesDetailsRepository extends JpaRepository<SupplyDetails,Long> {
    SupplyDetails  findAllById(Long id);

    List<SupplyDetails> getAllBySupplierCode(String supplierCode);

    List<SupplyDetails> getAllBySupplyNumber(String supplierNumber);

    @Query(value = "SELECT DISTINCT category, shape,thickness,finishing FROM supply_details where supplier_code = :supplierCode and material = :material", nativeQuery = true)
    List<SupplyDetailsProjection> getSuppliesDetailsByMaterial(@Param("supplierCode") String supplierCode, @Param("material") String material);

    @Query(value = "SELECT supply_code FROM supply_details where  material = :material and number > 0", nativeQuery = true)
    List<String> getAllSuppliesCodeByMaterial(String material);


    SupplyDetails getAllBySupplyCode(String id);


    List<SupplyDetails> getAllByMaterial(String material);

    @Query(value = "SELECT distinct(material) FROM supply_details", nativeQuery = true)
    List<String> getMaterials();

    @Query(value = "SELECT DISTINCT thickness FROM supply_details ORDER BY CAST(thickness AS DECIMAL(10,2)) ASC", nativeQuery = true)
    List<String> getThickness();

    @Query(value = "SELECT  number,total FROM supply_details where supply_code = :supplyCode" , nativeQuery = true)
    BalanceReport getDeductedNumberBySupplyCode(@Param("supplyCode") String supplyCode);

    @Query(value = """
        SELECT * FROM supply_details 
        WHERE (:material IS NULL OR material = :material)
        AND (:category IS NULL OR category = :category)
        AND (:shape IS NULL OR shape = :shape)
        AND (:finish IS NULL OR finishing = :finish)
        AND (:unit IS NULL OR unit = :unit)
        AND (:thickness IS NULL OR thickness = :thickness)
        """, nativeQuery = true)
    List<SupplyDetails> filterSupplyDetails(
            @Param("material") String material,
            @Param("category") String category,
            @Param("shape") String shape,
            @Param("finish") String finish,
            @Param("unit") String unit,
            @Param("thickness") String thickness
    );


}
