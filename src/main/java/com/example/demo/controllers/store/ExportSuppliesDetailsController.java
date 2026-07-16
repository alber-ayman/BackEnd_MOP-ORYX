package com.example.demo.controllers.store;

import com.example.demo.models.ExportSupplyDetails;
import com.example.demo.models.SupplyDetailsProjection;
import com.example.demo.service.store.ExportSuppliesDetailsService;
import com.example.demo.service.store.SupplySerialService;
import jakarta.servlet.http.HttpServletResponse;
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
@RequestMapping("/api/exportSuppliesDetails")
public class ExportSuppliesDetailsController {

    @Autowired
    ExportSuppliesDetailsService suppliesDetailsService;

    @Autowired
    SupplySerialService supplySerialService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<ExportSupplyDetails>> getAllSupplies(@RequestParam(name = "supplyNumber") String supplyNumber) throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getAllSuppliesDetails(supplyNumber);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byId/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<ExportSupplyDetails>> getSupplierById(@PathVariable("id") String id) throws ResourceNotFoundException, SQLException {
        try {
            return suppliesDetailsService.getAllSuppliesDetailsById(id);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save")  // Creating New JobOrders
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExportSupplyDetails> saveSupplier(
            @RequestBody ExportSupplyDetails suppliers) throws SQLException {
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

    @GetMapping("/materialsByProjectId/{projectId}")
    public ResponseEntity<List<String>> materialsByProjectId(HttpServletResponse response, @PathVariable(value = "projectId") String id
    ) {
        try {
            System.out.println("innnnn materialsByProjectId");
            List<String> materials = suppliesDetailsService.materialsByProjectId(id);

            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/materialsByProjectIdAndWorkOrder/{projectId}")
    public ResponseEntity<List<String>> materialsByProjectId(HttpServletResponse response, @PathVariable(value = "projectId") String id, @RequestParam(value = "workOrder") String workOrder )
    {
        try {
            List<String> materials = suppliesDetailsService.materialsByWorkOrder(id,workOrder);

            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/workOrderByProjectId/{projectId}")
    public ResponseEntity<List<String>> workOrderByProjectId(HttpServletResponse response, @PathVariable(value = "projectId") String projectId
    ) {
        try {
            List<String> workOrders = suppliesDetailsService.workOrderByProjectId(projectId);

            return ResponseEntity.ok(workOrders);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/exportedProjects")
    public ResponseEntity<List<String>> exportedProjects(HttpServletResponse response)
     {
        try {
            List<String> exportedProjects = suppliesDetailsService.getExportedProjects();

            return ResponseEntity.ok(exportedProjects);
        } catch (Exception e) {
            e.printStackTrace();
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

}
