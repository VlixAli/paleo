package com.VlixAli.paleo.repository;

import com.VlixAli.paleo.entity.EventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, UUID> {

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventId(UUID eventId);

    List<EventParticipant> findByEventId(UUID eventId);

    List<EventParticipant> findByUserId(UUID userId);
}
