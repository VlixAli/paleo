package com.VlixAli.paleo.validator;

import com.VlixAli.paleo.dto.request.EventCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EndTimeAfterStartTimeValidatorTest {

    private static Set<ConstraintViolation<EventCreateRequest>> validate(EventCreateRequest request) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(request);
        }
    }

    @Test
    void endAfterStartPasses() {
        var request = new EventCreateRequest("t", "d", "c", "l",
                Instant.parse("2030-01-01T10:00:00Z"), Instant.parse("2030-01-01T12:00:00Z"), null);
        assertThat(validate(request)).isEmpty();
    }

    @Test
    void endBeforeStartFailsOnEndTime() {
        var request = new EventCreateRequest("t", "d", "c", "l",
                Instant.parse("2030-01-01T12:00:00Z"), Instant.parse("2030-01-01T10:00:00Z"), null);
        assertThat(validate(request))
                .anyMatch(v -> v.getPropertyPath().toString().equals("endTime"));
    }

    @Test
    void nullTimesDeferredToNotNull() {
        var request = new EventCreateRequest("t", null, null, null, null, null, null);
        assertThat(validate(request)).noneMatch(v -> v.getMessage().equals("endTime must be after startTime"));
    }
}
