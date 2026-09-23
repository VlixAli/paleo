package com.VlixAli.paleo.repository;

import com.VlixAli.paleo.dto.response.ParticipantResponse;
import com.VlixAli.paleo.entity.EventParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
        select new com.VlixAli.paleo.dto.response.ParticipantResponse(
            u.id,
            u.displayName,
            ep.joinedAt
        )
        from EventParticipant ep
        join ep.user u
        where ep.event.id = :eventId
    """)
    Page<ParticipantResponse> findParticipantResponses(UUID eventId, Pageable pageable);

    List<EventParticipant> findByEventId(UUID eventId);

    List<EventParticipant> findByUserId(UUID userId);
}
