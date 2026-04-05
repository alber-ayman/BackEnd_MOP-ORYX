package com.example.demo.repository;

import com.example.demo.models.Suppliers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Suppliers,Long> {
    Suppliers findAllById(Long id);

    Suppliers getBySupplierName(String supplierName);

    Suppliers getBySupplierCode(String supplierName);
}
