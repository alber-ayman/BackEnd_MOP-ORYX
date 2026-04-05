package com.example.demo.service.store;

import com.example.demo.models.Suppliers;
import com.example.demo.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    @Autowired
    SupplierRepository supplierRepository;

    public ResponseEntity<List<Suppliers>> getAllSuppliers() {
        List<Suppliers> suppliers = supplierRepository.findAll();
        return new ResponseEntity<>(suppliers, HttpStatus.OK);
    }

    public Suppliers getSupplierById(Long id) {
        return supplierRepository.findAllById(id);
    }

    public ResponseEntity<Suppliers> addNewSupplier(Suppliers suppliers) {
        return new ResponseEntity<>(supplierRepository.save(suppliers),HttpStatus.OK);
    }

    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
}
