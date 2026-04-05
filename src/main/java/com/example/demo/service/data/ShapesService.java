package com.example.demo.service.data;

import com.example.demo.models.Shapes;
import com.example.demo.repository.ShapesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShapesService {

    @Autowired
    ShapesRepository shapesRepository;

    public ResponseEntity<List<Shapes>> getAllShapes() {
        return new ResponseEntity<>(shapesRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<Shapes> getShapesById(Long id){
        Shapes shapes = shapesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("officer Not Found for ID: " + id));

        return new ResponseEntity<>(shapes,HttpStatus.OK);
    }

    public ResponseEntity<Shapes> addNewShape(Shapes shapes) {

        return new ResponseEntity<>(shapesRepository.save(shapes),HttpStatus.OK);
    }

    public ResponseEntity<Shapes> updateShape(Long id, Shapes updatedShape) {
        Shapes shapes = shapesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Officer Not Found for ID: " + id));

        shapes.setShape(updatedShape.getShape());

        shapesRepository.save(shapes);
        return new ResponseEntity<>(shapes,HttpStatus.OK);
    }

    public void deleteShape(Long id) {
        shapesRepository.deleteById(id);
    }
}
