package com.learnhub.repository;

import com.learnhub.entity.NodeDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NodeDependencyRepository extends JpaRepository<NodeDependency, Long> {

    @Query("SELECT d FROM NodeDependency d WHERE d.fromNode.roadmap.id = :roadmapId")
    List<NodeDependency> findByRoadmapId(@Param("roadmapId") Long roadmapId);

    Optional<NodeDependency> findByFromNodeIdAndToNodeId(Long fromNodeId, Long toNodeId);

    void deleteByFromNodeIdAndToNodeId(Long fromNodeId, Long toNodeId);

    void deleteByFromNodeIdOrToNodeId(Long fromNodeId, Long toNodeId);
}
