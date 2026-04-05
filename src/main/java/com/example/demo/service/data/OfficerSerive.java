package com.example.demo.service.data;

import com.example.demo.models.Officer;
import com.example.demo.repository.OfficerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OfficerSerive {

    @Autowired
    OfficerRepository officerRepository;

    public ResponseEntity<List<Officer>> getAllOfficer() {
        return new ResponseEntity<>(officerRepository.findAll(), HttpStatus.OK);
    }

    public Officer getOfficerById(Long id){
        Officer officer = officerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("officer Not Found for ID: " + id));

        return officer;
    }

    public ResponseEntity<Officer> addNewOfficer(Officer officer) {

        return new ResponseEntity<>(officerRepository.save(officer),HttpStatus.OK);
    }

    public ResponseEntity<Officer> updateOfficer(Long id, Officer updatedOfficer) {
        Officer officer = officerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Officer Not Found for ID: " + id));

        officer.setOfficerName(updatedOfficer.getOfficerName());

        officerRepository.save(officer);
        return new ResponseEntity<>(officer,HttpStatus.OK);
    }

    public void deleteOfficer(Long id) {
        officerRepository.deleteById(id);
    }
}
