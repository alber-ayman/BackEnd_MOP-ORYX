package com.example.demo.controllers;

import com.example.demo.controllers.workOrder.pandsToJobOrderController;
import com.example.demo.models.ProjectProfile;
import com.example.demo.service.ProjectProfileService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
//@CrossOrigin(origins = "http://192.168.1.249:4200")
@CrossOrigin(origins = "*")
@RequestMapping("/api/projectprofile")
@Slf4j
public class ProjectProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectProfileController.class);


    @Autowired
    ProjectProfileService projectProfileService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ProjectProfile>> getAllProjectsProfile() throws SQLException {
        try {
            return projectProfileService.getAllProjectProfiles();
        } catch (Exception e) {
            logger.error("Error while processing request getAllProjectsProfile", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<ProjectProfile> getProjectById(@PathVariable("id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            ProjectProfile projectProfile = projectProfileService.getProjectProfileById(id);
            return new ResponseEntity<>(projectProfile, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allByName")
    @PreAuthorize("hasRole('USER') or hasRole('Viewer') or hasRole('ADMIN')")
    public ResponseEntity<ProjectProfile> getProjectByName(@RequestParam("name") String name) throws ResourceNotFoundException, SQLException {
        try {
            ProjectProfile projectProfile = projectProfileService.getProjectProfileByName(name);
            return new ResponseEntity<>(projectProfile, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save")  // Creating Project profile
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectProfile> saveProject(
            @RequestBody ProjectProfile projectProfile) throws SQLException {
        try {
            projectProfile.setSerial(1);
            projectProfile.setJobOrderSerial(1);
            return projectProfileService.addProjectProfile(projectProfile);
        } catch (Exception e) {
            logger.error("Error while processing request saveProject", e);
            return new ResponseEntity<>(projectProfile, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectProfile> updateMerchant(@PathVariable(value = "id") Long id, @RequestBody ProjectProfile updatedProject) throws ResourceNotFoundException, SQLException {
        try {
            return projectProfileService.updateProjectProfile(id,updatedProject);
        } catch (Exception e) {
            return new ResponseEntity<>(updatedProject, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMerchant(@PathVariable(value = "id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            return projectProfileService.deleteProjectProfile(id);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
