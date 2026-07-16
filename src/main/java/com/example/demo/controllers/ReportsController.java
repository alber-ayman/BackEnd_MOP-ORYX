package com.example.demo.controllers;

import com.example.demo.service.report.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;

//@CrossOrigin(origins = "http://192.168.1.249:4200")
@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    RestQuantityInPandsService restQuantityInPandsService;
    @Autowired
    RestQuantityForJobOrders restQuantityForJobOrders;

    @Autowired
    RestQuantityForRaws restQuantityForRaws;

    @Autowired
    QualityForJobOrder qualityForJobOrder;

    @Autowired
    WorkOrderAnalysis workOrdersAnalysis;


    @GetMapping("/download/{id}/{flag}")
    @PreAuthorize("hasRole('ADMIN')")
    public void downLoadExcel(HttpServletResponse response,
                              @PathVariable(value = "id") Long id,
                              @PathVariable(value = "flag") int flag,
                              @RequestParam(required = false) String fromDate,
                              @RequestParam(required = false) String toDate,
                              @RequestParam(required = false) String status) throws SQLException, IOException {
        try {
            String filename = "";
            ByteArrayInputStream inputStream = null;
            if (flag == 1) {
                filename = "/rest quantity" + ".xls";
                inputStream = restQuantityInPandsService.buildReport(id); // بالوحدات
            } else if (flag == 2) {
                filename = "/Work Order Details" + ".xls";
                inputStream = restQuantityForJobOrders.buildReport(id); //2*1
            } else if (flag == 3) {
                filename = "/RestQuantityForPands" + ".xls";
                inputStream = restQuantityForRaws.buildReport(id); // الخامات
            } else if (flag == 4){
                filename = "/Quantity Used" + ".xls";
                inputStream = qualityForJobOrder.buildReport(id); // كميه الخامات المستخدمة
            }else{
                filename = "/Work Orders Analysis" + ".xls";
//                inputStream = workOrdersAnalysis.buildReport(id);
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            IOUtils.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
        }
    }


    @GetMapping("/downloadPDF/{id}/{flag}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> downLoadPdf(HttpServletResponse response,
                                                           @PathVariable(value = "id") Long id,
                                                           @PathVariable(value = "flag") int flag,
                                                           @RequestParam(required = false) String fromDate,
                                                           @RequestParam(required = false) String toDate,
                                                           @RequestParam(required = false) String status) {
        try {
            InputStreamResource pdfBytes = null;
            if (flag == 1) {
                 pdfBytes = restQuantityInPandsService.getPdf(id); // بالوحدات
            } else if (flag == 2) {
                pdfBytes = restQuantityForJobOrders.getPdf(id); // 2*1
            } else if (flag == 3) {
                pdfBytes = restQuantityForRaws.getPdf(id); // الخامات
            } else if (flag == 4){
                pdfBytes = qualityForJobOrder.getPdf(id); // كميه الخامات المستخدمة
            }else{
                pdfBytes = workOrdersAnalysis.getPdf(id, fromDate, toDate, status);
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=converted.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
