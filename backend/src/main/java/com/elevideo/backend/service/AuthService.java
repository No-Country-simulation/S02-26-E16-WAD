package com.elevideo.backend.service;

import com.elevideo.backend.dto.auth.*;
import com.elevideo.backend.dto.user.UserRes;

public interface AuthService {

    /**
     * Registra un nuevo usuario en el sistema.
     * Genera un token de verificación y envía un email de confirmación.
     *
     * @param request Datos de registro del usuario
     * @return Información del usuario registrado
     * @throws com.elevideo.backend.exception.UserAlreadyExistsException si el email ya está registrado
     */
    UserRes register(RegisterReq request);

    /**
     * Autentica un usuario y genera un token JWT.
     *
     * @param request Credenciales de login
     * @return Token de autenticación JWT
     */
    LoginRes login(LoginReq request);

    VerifyEmailRes verifyEmail(VerifyEmailReq request);

    /**
     * Inicia el proceso de recuperación de contraseña.
     * Genera un token de reseteo y envía un email con instrucciones.
     *
     * @param request Email del usuario que solicita reseteo
     */
    void forgotPassword(ForgotPasswordReq request);

    /**
     * Restablece la contraseña del usuario usando un token válido.
     *
     * @param request Token y nueva contraseña
     */
    void resetPassword(ResetPasswordReq request);


}
