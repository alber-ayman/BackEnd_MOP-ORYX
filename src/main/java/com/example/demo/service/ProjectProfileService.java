package com.example.demo.service;

import com.example.demo.models.Pand;
import com.example.demo.models.ProjectProfile;
import com.example.demo.payload.excel.message.ResponseMessage;
import com.example.demo.payload.login.response.MessageResponse;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectProfileService.class);

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
            logger.error("Error while processing getAllProjectProfiles ",e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ProjectProfile getProjectProfileById(Long id) {
        ProjectProfile projectProfile = projectProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("project Not Found for ID: " + id));
        List<Pand> bands = pandsRepository.findByProjectProfileId(projectProfile.getId());
        if (!bands.isEmpty()) {
            projectProfile.setPands(bands);
        }
        return projectProfile;
    }

    public ProjectProfile getProjectProfileByName(String name) {
        ProjectProfile projectProfile = projectProfileRepository.findByProjectName(name)
                .orElseThrow(() -> new ResourceNotFoundException("project Not Found for : " + name));
        List<Pand> bands = pandsRepository.findByProjectProfileId(projectProfile.getId());
        if (!bands.isEmpty()) {
            projectProfile.setPands(bands);
        }
        return projectProfile;
    }

    public ResponseEntity<ProjectProfile> addProjectProfile(
            ProjectProfile projectProfile ) {
        try {
            Optional<ProjectProfile> existProjectProfile = projectProfileRepository.findByProjectName(projectProfile.getProjectName());

            if(existProjectProfile.isPresent()) {
                logger.error("Error while processing addProjectProfile projectName already exists");
                return new ResponseEntity<>(projectProfile, HttpStatus.BAD_REQUEST);
            }

            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = myDateObj.format(myFormatObj);

            projectProfile.setCreatedDate(formattedDate);
            projectProfileRepository.save(projectProfile);

            return new ResponseEntity<>(projectProfile, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while processing addProjectProfile",e);
            return new ResponseEntity<>(projectProfile, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<ProjectProfile> updateProjectProfile(Long id, ProjectProfile updatedProject) throws ResourceNotFoundException {
        try {
            Optional<ProjectProfile> project = projectProfileRepository.findById(id);

            if(project.isEmpty()) {
                return new ResponseEntity<>(updatedProject, HttpStatus.BAD_REQUEST);
            }

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
            logger.error("Error while processing getAllProjectProfiles ",e);
            return new ResponseEntity<>(updatedProject, HttpStatus.EXPECTATION_FAILED);
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

            return new ResponseEntity<>(new MessageResponse("Project has successfully Deleted ", 0), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while processing deleteProjectProfile ",e);
            return new ResponseEntity<>("Not Deleted", HttpStatus.EXPECTATION_FAILED);

        }
    }
}
