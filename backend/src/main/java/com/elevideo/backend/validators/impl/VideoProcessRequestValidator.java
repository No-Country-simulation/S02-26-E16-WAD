package com.elevideo.backend.validators.impl;

import com.elevideo.backend.dto.videoProcess.VideoProcessRequest;
import com.elevideo.backend.enums.ProcessingMode;
import com.elevideo.backend.validators.ValidVideoProcessRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador personalizado para VideoProcessRequest.
 *
 * Valida que los campos opcionales estén presentes cuando son requeridos
 * según el processingMode:
 *
 * - SHORT_MANUAL requiere: shortOptions (con startTime y duration)
 * - SHORT_AUTO requiere: shortAutoDuration
 * - VERTICAL no requiere campos adicionales
 */
public class VideoProcessRequestValidator
        implements ConstraintValidator<ValidVideoProcessRequest, VideoProcessRequest> {

    @Override
    public void initialize(ValidVideoProcessRequest constraintAnnotation) {
        // No se necesita inicialización
    }

    @Override
    public boolean isValid(VideoProcessRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // @NotNull se encarga de esto
        }

        ProcessingMode mode = request.processingMode();

        // Deshabilitar el mensaje de error por defecto
        context.disableDefaultConstraintViolation();

        switch (mode) {
            case SHORT_MANUAL:
                return validateShortManual(request, context);

            case SHORT_AUTO:
                return validateShortAuto(request, context);

            case VERTICAL:
                return validateVertical(request, context);

            default:
                context.buildConstraintViolationWithTemplate(
                        "Modo de procesamiento no soportado: " + mode
                ).addConstraintViolation();
                return false;
        }
    }

    private boolean validateShortManual(VideoProcessRequest request, ConstraintValidatorContext context) {
        if (request.shortOptions() == null) {
            context.buildConstraintViolationWithTemplate(
                    "El campo 'shortOptions' es requerido cuando processingMode = SHORT_MANUAL"
            ).addPropertyNode("shortOptions").addConstraintViolation();
            return false;
        }

        // Validar que startTime y duration estén presentes
        // (ya validados por @NotNull en el record interno, pero verificamos por si acaso)
        if (request.shortOptions().startTime() == null) {
            context.buildConstraintViolationWithTemplate(
                    "El campo 'shortOptions.startTime' es requerido"
            ).addPropertyNode("shortOptions.startTime").addConstraintViolation();
            return false;
        }

        if (request.shortOptions().duration() == null) {
            context.buildConstraintViolationWithTemplate(
                    "El campo 'shortOptions.duration' es requerido"
            ).addPropertyNode("shortOptions.duration").addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateShortAuto(VideoProcessRequest request, ConstraintValidatorContext context) {
        if (request.shortAutoDuration() == null) {
            context.buildConstraintViolationWithTemplate(
                    "El campo 'shortAutoDuration' es requerido cuando processingMode = SHORT_AUTO"
            ).addPropertyNode("shortAutoDuration").addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateVertical(VideoProcessRequest request, ConstraintValidatorContext context) {
        // VERTICAL no requiere campos adicionales
        // Solo validamos que los campos básicos estén presentes (ya validado por @NotNull)
        return true;
    }
}