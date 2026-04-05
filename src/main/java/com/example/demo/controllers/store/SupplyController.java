package com.example.demo.controllers.store;

import com.example.demo.models.ExportSupply;
import com.example.demo.models.Supply;
import com.example.demo.service.store.ExportSuppliesService;
import com.example.demo.service.store.SuppliesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/supply")
public class SupplyController {

    @Autowired
    SuppliesService supplyService;

    @Autowired
    ExportSuppliesService exportSuppliesService;

    public SupplyController(SuppliesService supplyService) {
        this.supplyService = supplyService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Supply>> getAllSupplies() throws SQLException {
        try {
            return supplyService.getAllSupplies();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byId/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<Supply> getSupplyById(@PathVariable("id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            Optional<Supply> supply = supplyService.getSupplyById(id);

            return new ResponseEntity<>(supply.get(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Supply> saveSupply(
            @RequestBody Supply supply,
            HttpServletRequest request ) throws SQLException {
        try {
            return supplyService.addNewSupply(supply,request);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(supply, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/exportSupply/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ExportSupply>> getAllExportSupplies() throws SQLException {
        try {
            return exportSuppliesService.getAllSupplies();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/exportSupply/byId/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<ExportSupply> getExportSupplyById(@PathVariable("id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            Optional<ExportSupply> supply = exportSuppliesService.getSupplyById(id);

            return new ResponseEntity<>(supply.get(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/exportSupply/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExportSupply> saveExportSupply(
            @RequestBody ExportSupply supply,
            HttpServletRequest request ) throws SQLException {
        try {
            return exportSuppliesService.addNewSupply(supply,request);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(supply, HttpStatus.BAD_REQUEST);
        }
    }
}
