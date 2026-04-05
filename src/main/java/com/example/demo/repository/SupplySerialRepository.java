package com.example.demo.repository;

import com.example.demo.models.SupplySerial;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplySerialRepository extends JpaRepository<SupplySerial, Long> {

    @Query("SELECT MAX(o.importSerial) FROM SupplySerial o")
    Integer findMaxImportSerial();

    @Query("SELECT MAX(o.exportSerial) FROM SupplySerial o")
    Integer findMaxExportSerial();

    @Modifying
    @Query("UPDATE SupplySerial s SET s.importSerial = s.importSerial + 1 WHERE s.id = 1")
    void increaseImportSerial();

    @Modifying
    @Query("UPDATE SupplySerial s SET s.exportSerial = s.exportSerial + 1 WHERE s.id = 1")
    void increaseExportSerial();
}
