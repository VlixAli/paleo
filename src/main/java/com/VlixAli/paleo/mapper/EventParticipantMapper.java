package com.VlixAli.paleo.mapper;

import com.VlixAli.paleo.dto.response.EventParticipantResponse;
import com.VlixAli.paleo.entity.EventParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventParticipantMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "userId", source = "user.id")
    EventParticipantResponse toResponse(EventParticipant participant);
}
