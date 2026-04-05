package com.example.demo.controllers.pand;

import com.example.demo.models.PandHistory;
import com.example.demo.service.pand.PandHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/pandHistory")
public class PandHistoryController {

    @Autowired
    PandHistoryService pandHistoryService;

    @GetMapping("/allById/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PandHistory>> getAllPandHistory(@PathVariable (name = "id") Long pandId) throws SQLException {
        try {
            return pandHistoryService.getAllPandHistory(pandId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/byId/{id}")
//    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
//    public ResponseEntity<PandHistory> getPandHistoryById(@PathVariable("id") Long id) throws ResourceNotFoundException, SQLException {
//        try {
//            PandHistory pandHistory = pandHistoryService.getPandHistoryById(id);
//
//            return new ResponseEntity<>(pandHistory, HttpStatus.OK);
//        } catch (Exception e) {
//
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PandHistory> savePandHistory(
            @RequestBody PandHistory pandHistory, HttpServletRequest request) throws SQLException {
        try {
            return pandHistoryService.addNewPandHistory(pandHistory,request);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PandHistory> updatePandHistory(@PathVariable(value = "id") Long id, @RequestBody PandHistory pandHistory) throws ResourceNotFoundException, SQLException {
        try {
            return pandHistoryService.updatePandHistory(id, pandHistory);
        } catch (Exception e) {
            return new ResponseEntity<>(pandHistory, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PandHistory> deletePandHistory(@PathVariable(value = "id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            return pandHistoryService.deletePandHistory(id);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
