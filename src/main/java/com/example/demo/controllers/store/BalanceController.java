package com.example.demo.controllers.store;

import com.example.demo.models.BalanceFilter;
import com.example.demo.models.MaterialFilter;
import com.example.demo.payload.FilterResponse;
import com.example.demo.service.store.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/balance")
public class BalanceController {

    @Autowired
    BalanceService balanceService;

    @PostMapping("/doFilter")
    public ResponseEntity<InputStreamResource> generateExcelToPdf(@RequestBody BalanceFilter balanceFilter) throws Exception {
        // 1. Create an Excel workbook in memory
        InputStreamResource pdfBytes = balanceService.getPdf(balanceFilter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=converted.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/showFilter")
    public ResponseEntity<List<FilterResponse>> returnFilterResult(@RequestBody BalanceFilter balanceFilter) throws Exception {
        List<FilterResponse> filterResponses = balanceService.getFilterResult(balanceFilter);

        return ResponseEntity.ok()
                .body(filterResponses);
    }

    @PostMapping("/projectBalance")
    public ResponseEntity<InputStreamResource> projectBalance(@RequestBody MaterialFilter materialFilter) throws Exception {
        // 1. Create an Excel workbook in memory
        InputStreamResource pdfBytes = balanceService.projectMaterialBalance(materialFilter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=converted.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/showProjectMaterialFilter")
    public ResponseEntity<List<FilterResponse>> returnFilterResult(@RequestBody MaterialFilter materialFilter) throws Exception {
        List<FilterResponse> filterResponses = balanceService.showProjectMaterialFilter(materialFilter);

        return ResponseEntity.ok()
                .body(filterResponses);
    }
}
