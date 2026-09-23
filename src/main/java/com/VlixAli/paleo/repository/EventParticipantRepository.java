package com.VlixAli.paleo.repository;

import com.VlixAli.paleo.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, UUID> {

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    @Modifying
    @Query("""
        DELETE FROM EventParticipant ep
        WHERE ep.event.id = :eventId
          AND ep.user.id = :userId
    """)
    int deleteByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventId(UUID eventId);

    List<EventParticipant> findByEventId(UUID eventId);

    List<EventParticipant> findByUserId(UUID userId);
}
