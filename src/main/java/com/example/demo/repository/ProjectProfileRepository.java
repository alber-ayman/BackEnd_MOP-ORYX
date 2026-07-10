package com.example.demo.repository;

import com.example.demo.models.ProjectProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectProfileRepository extends JpaRepository<ProjectProfile,Long> {
    Optional<ProjectProfile> findByProjectName(String name);
}
