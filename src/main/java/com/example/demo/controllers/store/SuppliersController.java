package com.example.demo.controllers.store;

import com.example.demo.models.Suppliers;
import com.example.demo.service.store.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/suppliers")
public class SuppliersController {

    @Autowired
    SupplierService supplierService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Suppliers>> getAllOfficer() throws SQLException {
        try {
            return supplierService.getAllSuppliers();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byId/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<Suppliers> getSupplierById(@PathVariable("id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            Suppliers supplier = supplierService.getSupplierById(id);

            return new ResponseEntity<>(supplier, HttpStatus.OK);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save")  // Creating New JobOrders
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Suppliers> saveSupplier(
            @RequestBody Suppliers suppliers) throws SQLException {
        try {
            return supplierService.addNewSupplier(suppliers);
        } catch (Exception e) {
            return new ResponseEntity<>(suppliers, HttpStatus.BAD_REQUEST);
        }
    }
//
//    @PutMapping("/update/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Suppliers> updateOfficer(@PathVariable(value = "id") Long id, @RequestBody Suppliers officer) throws ResourceNotFoundException, SQLException {
//        try {
//            return supplierService.updateSupplier(id,officer);
//        } catch (Exception e) {
//            return new ResponseEntity<>(officer, HttpStatus.BAD_REQUEST);
//        }
//    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteSupplier(@PathVariable(value = "id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            supplierService.deleteSupplier(id);
            return new ResponseEntity<>("Deleted Successfully", HttpStatus.OK);


        } catch (Exception e) {
            return new ResponseEntity<>("Exception", HttpStatus.BAD_REQUEST);
        }
    }

}
