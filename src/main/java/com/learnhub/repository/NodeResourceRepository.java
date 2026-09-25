package com.learnhub.repository;

import com.learnhub.entity.NodeResource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NodeResourceRepository extends JpaRepository<NodeResource, Long> {

    List<NodeResource> findByNodeIdOrderByOrderIndexAsc(Long nodeId);

    void deleteByNodeId(Long nodeId);
}
