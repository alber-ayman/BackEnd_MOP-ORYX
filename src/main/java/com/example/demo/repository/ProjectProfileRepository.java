package com.example.demo.repository;

import com.example.demo.models.ProjectProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectProfileRepository extends JpaRepository<ProjectProfile,Long> {
    ProjectProfile getById(Long projectCode);
}
