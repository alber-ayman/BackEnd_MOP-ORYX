package com.example.demo.controllers;

import com.example.demo.DTO.MaterialIssueRequestDto;
import com.example.demo.models.JobOrder;
import com.example.demo.models.MaterialIssueRequest;
import com.example.demo.repository.JobOrderRepository;
import com.example.demo.repository.MaterialIssueRequestReporitory;
import com.example.demo.service.MaterialIssuerService;
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

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/file")
public class MaterialIssuerController {

    MaterialIssuerService materialIssuerService;

    MaterialIssueRequestReporitory materialIssueRequestReporitory;

    JobOrderRepository jobOrderRepository;

    public MaterialIssuerController(MaterialIssuerService materialIssuerService, MaterialIssueRequestReporitory materialIssueRequestReporitory,JobOrderRepository jobOrderRepository) {
        this.materialIssuerService = materialIssuerService;
        this.materialIssueRequestReporitory = materialIssueRequestReporitory;
        this.jobOrderRepository = jobOrderRepository;
    }

    @PostMapping("/exportMaterialIssuer")
    public ResponseEntity<byte[]> export(
            @RequestBody MaterialIssueRequestDto dto)
            throws Exception {

        byte[] pdf = materialIssuerService.export(dto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=material-request.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/exportSalesMaterialIssuer")
    public ResponseEntity<byte[]> generateExcelToPdf(@RequestParam(name = "jobOrderNumber") String id) throws Exception {
        // 1. Create an Excel workbook in memory
        byte[] pdf = materialIssuerService.exportToSales(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=converted.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/getSalesMaterialIssuer")
    public ResponseEntity<List<MaterialIssueRequest>> getSalesMaterialIssuer(@RequestParam(name = "jobOrderNumber") Long workOrder) throws Exception {
        Optional<JobOrder> jobOrder = jobOrderRepository.findById(workOrder);
        return new ResponseEntity<>(materialIssueRequestReporitory.findAllByWorkOrderNo(jobOrder.get().getJobOrderNumber()), HttpStatus.OK);
    }
}
