package com.learnhub.repository;

import com.learnhub.entity.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {

    List<RoadmapNode> findByRoadmapIdOrderByOrderIndexAsc(Long roadmapId);

    long countByRoadmapId(Long roadmapId);
}
