package com.example.demo.controllers.data;

import com.example.demo.models.Shapes;
import com.example.demo.service.data.ShapesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

//@CrossOrigin(origins = "http://192.168.1.249:4200")
@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/shapes")
public class ShapesController {

    @Autowired
    ShapesService shapesService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Shapes>> getAllShapes() throws SQLException {
        try {
            return shapesService.getAllShapes();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Shapes> getShapesById(@PathVariable(value = "id") Long id) throws SQLException {
        try {
            return shapesService.getShapesById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save")  // Creating New Pand
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Shapes> saveShape(
            @RequestBody Shapes shape) throws SQLException {
        try {
            return shapesService.addNewShape(shape);
        } catch (Exception e) {
            return new ResponseEntity<>(shape, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Shapes> updateShape(@PathVariable(value = "id") Long id, @RequestBody Shapes shape) throws ResourceNotFoundException, SQLException {
        try {
            return shapesService.updateShape(id,shape);
        } catch (Exception e) {
            return new ResponseEntity<>(shape, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteShape(@PathVariable(value = "id") Long id) throws ResourceNotFoundException, SQLException {
        try {
            shapesService.deleteShape(id);
            return new ResponseEntity<>("Deleted Successfully", HttpStatus.OK);


        } catch (Exception e) {
            return new ResponseEntity<>("Exception", HttpStatus.BAD_REQUEST);
        }
    }
}
