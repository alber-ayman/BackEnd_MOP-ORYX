package com.example.demo.controllers.workOrder;

import com.example.demo.models.FileDB;
import com.example.demo.models.JobOrder;
import com.example.demo.payload.CheckLimitResponse;
import com.example.demo.payload.SendToBody;
import com.example.demo.repository.FileDBRepository;
import com.example.demo.repository.JobOrderRepository;
import com.example.demo.service.workOrder.JobOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/api/jobOrder")
public class JobOrderController {

    @Autowired
    JobOrderService jobOrderService;

    @Autowired
    JobOrderRepository jobOrderRepository;

    @Autowired
    private FileDBRepository fileDBRepository;

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<JobOrder>> getAllJobOrders() throws SQLException {
        try {
            return jobOrderService.getAllJobOrders();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byId/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<JobOrder> getJobOrdersById(@PathVariable("id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            JobOrder jobOrder = jobOrderService.getJobOrderById(id);

            return new ResponseEntity<>(jobOrder, HttpStatus.OK);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byProjectId/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<JobOrder>> getJobOrdersByProjectId(@PathVariable("id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            List<JobOrder> jobOrder = jobOrderService.getByProjectId(id);

            return new ResponseEntity<>(jobOrder, HttpStatus.OK);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/byProjectCode")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<List<JobOrder>> getJobOrdersByProjectCode(@RequestParam("projectCode") String id) throws ResourceNotFoundException, SQLException {
        try {
            List<JobOrder> jobOrder = jobOrderService.getByProjectCode(id);

            return new ResponseEntity<>(jobOrder, HttpStatus.OK);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/save")  // Creating New JobOrders
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobOrder> saveJobOrders(
            @RequestBody JobOrder jobOrder, HttpServletRequest request) throws SQLException {
        try {
            return jobOrderService.addNewJobOrder(jobOrder,request);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(jobOrder, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobOrder> updateJobOrders(@PathVariable(value = "id") Long id, @RequestBody JobOrder jobOrder, HttpServletRequest request) throws ResourceNotFoundException, SQLException {
        try {
            return jobOrderService.updateJobOrder(id, jobOrder,request);
        } catch (Exception e) {
            return new ResponseEntity<>(jobOrder, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/updateQuantity/{id}/{flag}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobOrder> updateQuantity(@PathVariable(value = "id") Long id, @RequestBody JobOrder jobOrder, @PathVariable(value = "flag") int flag, HttpServletRequest request) throws ResourceNotFoundException, SQLException {
        try {
            return jobOrderService.updateJobOrder(id, jobOrder, request);
        } catch (Exception e) {
            return new ResponseEntity<>(jobOrder, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteJobOrders(@PathVariable(value = "id") Long id, HttpServletRequest request) throws ResourceNotFoundException, SQLException {
        try {
            return jobOrderService.deleteJobOrder(id, request);
        } catch (Exception e) {
            return new ResponseEntity<>("Exception", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/upload/{id}")
    public ResponseEntity<CheckLimitResponse> handleFileUpload(@RequestParam("file") MultipartFile file, @PathVariable(value = "id") Long id) {
        try {
            FileDB fileEntity = new FileDB();
            fileEntity.setName(file.getOriginalFilename());
            fileEntity.setData(file.getBytes());
            fileEntity.setJobOrderId(id);

            fileDBRepository.save(fileEntity);
            String message = "File uploaded successfully with ID: " + fileEntity.getId();
            Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);
            jobOrder.get().setFileId(fileEntity.getId());
            jobOrderRepository.save(jobOrder.get());
            int flag = 1;
            CheckLimitResponse messageResponse = new CheckLimitResponse(message, flag);

            return new ResponseEntity<>(messageResponse, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            String message = "File upload failed";
            int flag = 0;
            CheckLimitResponse messageResponse = new CheckLimitResponse(message, flag);
            return new ResponseEntity<>(messageResponse, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Transactional
    @PostMapping("/copy")  // Creating New JobOrders
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobOrder> copyJobOrders(
            @RequestParam(name = "jobOrderNumber") String jobOrder) throws SQLException {
        JobOrder copiedJobOrder = new JobOrder();
        try {
            copiedJobOrder = jobOrderService.copyJobORder(jobOrder);
            return new ResponseEntity<>(copiedJobOrder,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(copiedJobOrder, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/jobOrderDetails/{id}")
    public  ResponseEntity<InputStreamResource> getJobOrderDetails(HttpServletResponse response, @PathVariable(value = "id") Long id
            ){
        try {
            InputStreamResource pdfBytes = jobOrderService.getJobOrderDetails(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=converted.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/jobOrderSearch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobOrder> searchJobOrders(
            @RequestParam(name = "jobOrderNumber") String jobOrder) throws SQLException {
        JobOrder copiedJobOrder = new JobOrder();
        try {
            copiedJobOrder = jobOrderService.getByJobOrder(jobOrder);
            return new ResponseEntity<>(copiedJobOrder,HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(copiedJobOrder, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/approveWorkOrder/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobOrder> approveWorkOrder(
            @PathVariable(name = "id") Long jobOrderID) throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.approveWorkOrder(jobOrderID),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/revertWorkOrder/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobOrder> revertWorkOrder(
            @PathVariable(name = "id") Long jobOrderID) throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.revertWorkOrder(jobOrderID),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getPending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobOrder>> getPending() throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.getPendingJobOrder(),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getManufacturingPending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobOrder>> getManufacturingPending() throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.getPendingManufacturingJobOrder(),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getStorePending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobOrder>> getStorePending() throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.getPendingStoreJobOrder(),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getPurchasingPending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobOrder>> getPurchasingPending() throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.getPendingPurchaseJobOrder(),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getReverted/{userName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<JobOrder>> getReverted(@PathVariable(name = "userName")String userName) throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.getRevertedJobOrder(userName),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/sendToUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobOrder> sendToUser(@RequestBody SendToBody sendToBody, @PathVariable(name = "id")Long id) throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.sendJobOrderToUser(sendToBody,id),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/storeApprove/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveManufacturingPending(@PathVariable(name = "id") Long id) throws SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.approveStoreJobOrder(id),HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something Wrong while store approve", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/incrementSerial/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<JobOrder> incrementSerial(@PathVariable("id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            return new ResponseEntity<>(jobOrderService.commitJobOrder(id), HttpStatus.OK);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/uploadImage/{id}")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            @PathVariable("id") Long id) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file selected");
        }

        try {
            Optional<JobOrder> jobOrder = jobOrderRepository.findById(id);
            jobOrder.get().setImageDescription(description);
            jobOrder.get().setImage(file.getBytes());

            jobOrderRepository.save(jobOrder.get());
            return ResponseEntity.ok("Image saved to DB successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving image: " + e.getMessage());
        }
    }

    @GetMapping("/jobOrderImage/delete/{jobOrderId}")
    public ResponseEntity<String> deletePandImage(HttpServletResponse response, @PathVariable(value = "jobOrderId") Long id
    ) {
        try {
            String message = jobOrderService.deleteJobOrderImage(id);

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
