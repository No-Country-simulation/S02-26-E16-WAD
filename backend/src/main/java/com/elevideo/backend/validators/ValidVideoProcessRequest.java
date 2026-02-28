package com.elevideo.backend.validators;

import com.elevideo.backend.validators.impl.VideoProcessRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación para validar que VideoProcessRequest tenga los campos requeridos
 * según su processingMode.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VideoProcessRequestValidator.class)
@Documented
public @interface ValidVideoProcessRequest {

    String message() default "La solicitud de procesamiento no es válida para el modo seleccionado";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}