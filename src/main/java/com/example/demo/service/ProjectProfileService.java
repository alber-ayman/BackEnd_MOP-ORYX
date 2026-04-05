package com.example.demo.service;

import com.example.demo.models.JobOrder;
import com.example.demo.models.Pand;
import com.example.demo.models.ProjectProfile;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectProfileService {

    @Autowired
    ProjectProfileRepository projectProfileRepository;

    @Autowired
    PandsRepository pandsRepository;

    @Autowired
    ExitJobOrderRepository exitJobOrderRepository;

    @Autowired
    PandsToJobOrderRepository pandsToJobOrderRepository;

    @Autowired
    JobOrderRepository jobOrderRepository;

    public ResponseEntity<List<ProjectProfile>> getAllProjectProfiles() {
        try {
            List<ProjectProfile> projectProfiles = projectProfileRepository.findAll();

            if (projectProfiles.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(projectProfiles, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ProjectProfile getProjectProfileById(Long id) {
        ProjectProfile projectProfile = projectProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("project Not Found for ID: " + id));

        List<Pand> pands = pandsRepository.findByProjectProfileId(projectProfile.getId());

        if (!pands.isEmpty()) {
            projectProfile.setPands(pands);
        }

        return projectProfile;
    }

    public ResponseEntity<ProjectProfile> addProjectProfile(
            ProjectProfile projectProfile) throws SQLException {
        try {
//            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//            projectProfile.setCreatedBy(userDetails.getUsername());

            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = myDateObj.format(myFormatObj);

            projectProfile.setCreatedDate(formattedDate);
            projectProfileRepository.save(projectProfile);

            return new ResponseEntity<>(projectProfile, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(projectProfile, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<ProjectProfile> updateProjectProfile(Long id, ProjectProfile updatedProject) throws ResourceNotFoundException, SQLException {
        try {
            Optional<ProjectProfile> project = projectProfileRepository.findById(id);


            project.get().setProjectCode(updatedProject.getProjectCode());
            project.get().setProjectName(updatedProject.getProjectName());
            project.get().setAddress(updatedProject.getAddress());

            project.get().setEngineerName(updatedProject.getEngineerName());

            project.get().setCreatedBy(updatedProject.getCreatedBy());
            project.get().setCreatedDate(updatedProject.getCreatedDate());
            project.get().setEmail(updatedProject.getEmail());
            project.get().setContractor(updatedProject.getContractor());
            project.get().setMobile(updatedProject.getMobile());
            project.get().setStartDate(updatedProject.getStartDate());

            projectProfileRepository.save(project.get());
            return new ResponseEntity<>(project.get(), HttpStatus.OK);
        } catch (Exception e) {
            return null;
        }
    }


    @Transactional
    public ResponseEntity<?> deleteProjectProfile(Long id) {
        try {

            exitJobOrderRepository.deleteByProjectProfileId(id);

            jobOrderRepository.deleteByProjectProfileId(id);

            pandsToJobOrderRepository.deleteByProjectProfileId(id);

            pandsRepository.deleteByProjectProfileId(id);

            projectProfileRepository.deleteById(id);

            return new ResponseEntity<>("Deleted Successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Not Deleted", HttpStatus.EXPECTATION_FAILED);

        }
    }
}
