package com.example.demo.repository;

import com.example.demo.models.ExportSupply;
import com.example.demo.models.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExportSupplyRepository extends JpaRepository<ExportSupply,Long> {

    Optional<ExportSupply> findAllById(Long id);

    ExportSupply getBySupplyNumber(String supplyCode);
}
