package com.example.demo.controllers.store;

import com.example.demo.models.SupplyDetails;
import com.example.demo.models.SupplyDetailsProjection;
import com.example.demo.service.store.SuppliesDetailsService;
import com.example.demo.service.store.SupplySerialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/suppliesDetails")
public class SuppliesDetailsController {

    @Autowired
    SuppliesDetailsService suppliesDetailsService;

    @Autowired
    SupplySerialService supplySerialService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<SupplyDetails>> getAllSupplies(@RequestParam(name = "supplyNumber") String supplyNumber) throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getAllSuppliesDetails(supplyNumber);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byId/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<SupplyDetails>> getSupplierById(@PathVariable("id") String id) throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getAllSuppliesDetailsById(id);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save")  // Creating New JobOrders
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplyDetails> saveSupplier(
            @RequestBody SupplyDetails suppliers) throws SQLException {
        try {
            return suppliesDetailsService.addNewSupplydetails(suppliers);
        } catch (Exception e) {
            return new ResponseEntity<>(suppliers, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteSupplier(@PathVariable(value = "id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            suppliesDetailsService.deleteSupplier(id);
            return new ResponseEntity<>("Deleted Successfully", HttpStatus.OK);


        } catch (Exception e) {
            return new ResponseEntity<>("Exception", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getDetails/{id}/{materialName}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<SupplyDetailsProjection>> getDetailsByMaterial(@PathVariable("id") String id, @PathVariable("materialName") String materialName) throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getSuppliesDetailsByMaterial(id,materialName);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/importSerial")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public String importSerial() throws ResourceNotFoundException, SQLException {
        try {
             supplySerialService.increaseImportSerial();
            return "Imported Successfully";
        } catch (Exception e) {

            return null;
        }
    }

    @GetMapping("/exportSerial")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public String exportSerial() throws ResourceNotFoundException, SQLException {
        try {
            supplySerialService.increaseExportSerial();
            return "Imported Successfully";
        } catch (Exception e) {

            return null;
        }
    }

    @PostMapping("/generate/suppliesFile")
    public ResponseEntity<InputStreamResource> generateExcelToPdf(@RequestParam(name = "supplyNumber") String id) throws Exception {
        // 1. Create an Excel workbook in memory
        InputStreamResource pdfBytes = suppliesDetailsService.getPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=converted.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/suppliesCode/byMaterialName/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getSupplyCodeByMaterialId(@PathVariable("id") String id) throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getSuppliesCode(id);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/bySupplyCode/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<SupplyDetails> getSupplyDetailsBySupplyCode(@PathVariable("id") String id) throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getSupplyDetailsBySupplyCode(id);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/thickness")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getthickness() throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getThickness();
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/importedMaterials")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<String>> getImportedMaterials() throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getImportedMaterials();
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
