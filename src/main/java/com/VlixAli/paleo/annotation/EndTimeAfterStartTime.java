package com.VlixAli.paleo.annotation;

import com.VlixAli.paleo.validator.EndTimeAfterStartTimeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EndTimeAfterStartTimeValidator.class)
public @interface EndTimeAfterStartTime {
    String message() default "endTime must be after startTime";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default {};
}
