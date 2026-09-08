package com.VlixAli.paleo.validator;

import com.VlixAli.paleo.annotation.EndTimeAfterStartTime;
import com.VlixAli.paleo.dto.request.EventTimeRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndTimeAfterStartTimeValidator implements ConstraintValidator<EndTimeAfterStartTime, EventTimeRange> {

    public void initialize(EndTimeAfterStartTime parameters) {
        // Nothing to do here
    }

    public boolean isValid(EventTimeRange request, ConstraintValidatorContext context) {
        if (request == null || request.startTime() == null || request.endTime() == null) {
            return true;
        }
        if (request.endTime().isAfter(request.startTime())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("endTime")
                .addConstraintViolation();
        return false;
    }
}
