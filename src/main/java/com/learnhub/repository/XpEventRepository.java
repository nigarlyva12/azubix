package com.learnhub.repository;

import com.learnhub.entity.User;
import com.learnhub.entity.XpEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface XpEventRepository extends JpaRepository<XpEvent, Long> {

    /** Deduplication — was this specific action already rewarded? */
    boolean existsByUserAndReasonAndReferenceId(User user, String reason, Long referenceId);

    /** Count how many times a particular action was rewarded (e.g. ARTICLE_READ count). */
    long countByUserAndReason(User user, String reason);

    /** Daily-login deduplication: any DAILY_LOGIN event in the last 24 h window. */
    boolean existsByUserAndReasonAndCreatedAtBetween(
            User user, String reason,
            LocalDateTime from, LocalDateTime to);
}
