package com.example.demo.repository;

import com.example.demo.models.PandHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PandHistoryRepository extends JpaRepository<PandHistory, Long> {

    List<PandHistory> getByPandId(Long id);
}
