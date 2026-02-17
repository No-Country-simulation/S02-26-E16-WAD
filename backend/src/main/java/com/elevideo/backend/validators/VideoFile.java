package com.elevideo.backend.validators;

import com.elevideo.backend.validators.impl.VideoFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = VideoFileValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VideoFile {
    String message() default "El archivo debe ser un archivo de música válido (mp3, wav, flac)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}