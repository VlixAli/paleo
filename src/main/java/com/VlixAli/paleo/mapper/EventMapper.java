package com.VlixAli.paleo.mapper;

import com.VlixAli.paleo.dto.request.EventCreateRequest;
import com.VlixAli.paleo.dto.request.EventUpdateRequest;
import com.VlixAli.paleo.dto.response.EventResponse;
import com.VlixAli.paleo.entity.Event;
import com.VlixAli.paleo.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "owner", source = "owner.id")
    EventResponse eventToEventResponse(Event event);

    @Mapping(target = "owner", source = "creator")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventCreateRequest request, User creator);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEventFromRequest(EventUpdateRequest request, @MappingTarget Event event);
}
