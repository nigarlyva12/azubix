package com.learnhub.repository;

import com.learnhub.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    List<Roadmap> findByPublishedTrueOrderByExamNameAscPlanTypeAsc();

    List<Roadmap> findByExamNameOrderByPlanTypeAsc(String examName);

    @Query("SELECT DISTINCT r.examName FROM Roadmap r WHERE r.published = true ORDER BY r.examName ASC")
    List<String> findDistinctPublishedExamNames();

    @Query("SELECT DISTINCT r.examName FROM Roadmap r ORDER BY r.examName ASC")
    List<String> findDistinctExamNames();
}
