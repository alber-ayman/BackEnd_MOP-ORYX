package com.example.demo.controllers.workOrder;

import com.example.demo.models.ExitProcessJobOrder;
import com.example.demo.models.JobOrderParent;
import com.example.demo.service.workOrder.ExitProcessJobOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

//@CrossOrigin(origins = "http://192.168.1.249:4200")
@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/exitProcessJobOrder")
public class ExitProcessJobOrderController {

    @Autowired
    ExitProcessJobOrderService jobOrderService;

    @PostMapping("/save/{serial}")  // Creating Project profile
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExitProcessJobOrder>> saveChildPand(
            @PathVariable("serial") String unifiedSerial,
            @RequestBody JobOrderParent jobOrderParent
            ) throws SQLException {
        try {
            return jobOrderService.saveExitProcessJobOrder(jobOrderParent,unifiedSerial);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
