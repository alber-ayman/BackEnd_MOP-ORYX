package com.example.demo.repository;

import com.example.demo.models.Supply;
import com.example.demo.models.SupplyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyRepository extends JpaRepository<Supply,Long> {

    Optional<Supply> findAllById(Long id);

    Supply getAllBySupplyNumber(String supplierNumber);
}
