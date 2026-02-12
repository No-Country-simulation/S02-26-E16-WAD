package com.elevideo.backend.controller;

import com.elevideo.backend.documentation.auth.*;
import com.elevideo.backend.dto.ApiResult;
import com.elevideo.backend.dto.auth.*;
import com.elevideo.backend.dto.user.UserRes;
import com.elevideo.backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "01 - Autenticación",
        description = "Endpoints para autenticación de usuarios y gestión de cuentas")
public class AuthController {

    private final AuthService authService;

    @RegisterEndpointDoc
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterReq request) {
        UserRes response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success(response,"Registro completado con éxito"));
    }

    @LoginEndpointDoc
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginReq request) {
        LoginRes response = authService.login(request);
        return ResponseEntity.ok(ApiResult.success(response, "Inicio de sesión exitoso"));
    }

    @VerifyEmailEndpointDoc
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid VerifyEmailReq request) {
        VerifyEmailRes response = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResult.success( response,"Email verificado exitosamente"));
    }

    @ForgotPasswordEndpointDoc
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordReq request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResult.success("Se ha enviado un correo para restablecer tu contraseña"));
    }

    @ResetPasswordEndpointDoc
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordReq request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResult.success("Contraseña restablecida correctamente"));
    }


}
