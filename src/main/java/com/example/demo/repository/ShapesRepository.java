package com.example.demo.repository;

import com.example.demo.models.FinishTypes;
import com.example.demo.models.Shapes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShapesRepository extends JpaRepository<Shapes,Long> {
}
