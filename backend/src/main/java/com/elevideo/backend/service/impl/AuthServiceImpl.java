package com.elevideo.backend.service.impl;

import com.elevideo.backend.aspect.LogExecution;
import com.elevideo.backend.dto.JwtDataDto;
import com.elevideo.backend.dto.auth.*;
import com.elevideo.backend.dto.user.UserRes;
import com.elevideo.backend.enums.TokenPurpose;
import com.elevideo.backend.exception.EmailAlreadyVerifiedException;
import com.elevideo.backend.exception.UserAlreadyExistsException;
import com.elevideo.backend.exception.UserNotFoundException;
import com.elevideo.backend.mapper.UserMapper;
import com.elevideo.backend.model.User;
import com.elevideo.backend.repository.UserRepository;
import com.elevideo.backend.security.CustomUserDetails;
import com.elevideo.backend.security.JwtService;
import com.elevideo.backend.service.AuthService;
import com.elevideo.backend.service.EmailService;
import com.elevideo.backend.service.TokenBlacklistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // --- Configuration Properties ---
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.verify-email-path}")
    private String verifyEmailPath;

    @Value("${app.frontend.reset-password-path}")
    private String resetPasswordPath;

    // --- Services ---
    private final EmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    // --- Security & Auth ---
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    // --- Repositories & Mappers ---
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // --- Date Formatter ---
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @LogExecution
    @Override
    @Transactional
    public UserRes register(RegisterReq request) {
        validateUserDoesNotExist(request.email());

        User user = createAndSaveUser(request);

        String verificationToken = generateVerificationToken(user);
        sendVerificationEmail(user, verificationToken);

        return userMapper.toUserRes(user);
    }

    @Override
    public LoginRes login(LoginReq request) {
        Authentication authentication = authenticateUser(request);
        User user = extractUserFromAuthentication(authentication);

        validateEmailIsVerified(user);

        String accessToken = generateAccessToken(user);
        return new LoginRes(accessToken);
    }

    @Override
    @Transactional
    public VerifyEmailRes verifyEmail(VerifyEmailReq request) {
        jwtService.validateEmailVerificationToken(request.token());
        UUID userId = jwtService.extractUserId(request.token());

        User user = findUserById(userId);

        validateEmailNotAlreadyVerified(user);

        verifyUserEmail(user);
        blacklistToken(request.token());

        return new VerifyEmailRes(user.getEmail(),user.isEmailVerified());
    }

    @Override
    public void forgotPassword(ForgotPasswordReq request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String resetToken = generatePasswordResetToken(user);
            sendPasswordResetEmail(user, resetToken);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordReq request) {
        jwtService.validateResetPasswordToken(request.token());
        UUID userId = jwtService.extractUserId(request.token());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        updateUserPassword(user,request.newPassword());
    }






    // ==================== MÉTODOS PRIVADOS - VALIDACIÓN ====================

    /**
     * Valida que el email no esté ya registrado.
     */
    private void validateUserDoesNotExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }
    }

    /**
     * Valida que el email no haya sido verificado previamente.
     */
    private void validateEmailNotAlreadyVerified(User user) {
        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("El email ya ha sido verificado previamente");
        }
    }

    /**
     * Verifica que el correo del usuario ya haya sido verificado.
     */
    private void validateEmailIsVerified(User user) {
        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Debes verificar tu email antes de iniciar sesión");
        }
    }

    // ==================== MÉTODOS PRIVADOS - USUARIO ====================

    /**
     * Crea y persiste un nuevo usuario.
     */
    private User createAndSaveUser(RegisterReq request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toUser(request, encodedPassword);
        return userRepository.save(user);
    }

    /**
     * Busca un usuario por ID o lanza excepción si no existe.
     */
    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("No se encontró el usuario con ID: %s", userId)
                ));
    }

    /**
     * Actualiza la contraseña del usuario.
     */
    private void updateUserPassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    /**
     * Marca el email del usuario como verificado.
     */
    private void verifyUserEmail(User user) {
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    // ==================== MÉTODOS PRIVADOS - TOKENS ====================

    /**
     * Genera token de verificación de email.
     */
    private String generateVerificationToken(User user) {
        JwtDataDto jwtData = userMapper.toJwtDataDto(user);
        return jwtService.generateToken(jwtData, TokenPurpose.EMAIL_VERIFICATION);
    }

    /**
     * Genera token de autenticación (access token).
     */
    private String generateAccessToken(User user) {
        JwtDataDto jwtData = userMapper.toJwtDataDto(user);
        return jwtService.generateToken(jwtData, TokenPurpose.AUTHENTICATION);
    }

    /**
     * Genera token de reseteo de contraseña.
     */
    private String generatePasswordResetToken(User user) {
        JwtDataDto jwtData = userMapper.toJwtDataDto(user);
        return jwtService.generateToken(jwtData, TokenPurpose.PASSWORD_RESET);
    }

    /**
     * Agrega el token usado a la lista negra para prevenir reutilización.
     */
    private void blacklistToken(String token) {
        LocalDateTime expiration = jwtService.extractExpiration(token);
        tokenBlacklistService.addTokenToBlacklist(token, expiration);
    }

    // ==================== MÉTODOS PRIVADOS - AUTENTICACIÓN ====================

    /**
     * Autentica las credenciales del usuario.
     */
    private Authentication authenticateUser(LoginReq request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
    }

    /**
     * Extrae el usuario del objeto de autenticación.
     */
    private User extractUserFromAuthentication(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    // ==================== MÉTODOS PRIVADOS - EMAIL ====================

    /**
     * Envía email de verificación al usuario.
     */
    private void sendVerificationEmail(User user, String token) {
        String verificationUrl = buildVerificationUrl(token);
        String fullName = String.format("%s %s", user.getFirstName(), user.getLastName());
        String formattedDate = user.getCreatedAt().format(DATE_FORMATTER);

        Map<String, Object> variables = Map.of(
                "userName", fullName,
                "userEmail", user.getEmail(),
                "registrationDate", formattedDate,
                "verificationLink", verificationUrl
        );

        emailService.sendEmailTemplate(
                user.getEmail(),
                "Verifica tu cuenta en Elevideo",
                "emails/welcome-email",
                variables
        );
    }

    /**
     * Envía email de recuperación de contraseña.
     */
    private void sendPasswordResetEmail(User user, String token) {
        String resetUrl = buildPasswordResetUrl(token);
        String fullName = String.format("%s %s", user.getFirstName(), user.getLastName());

        Map<String, Object> variables = Map.of(
                "userName", fullName,
                "resetLink", resetUrl
        );

        emailService.sendEmailTemplate(
                user.getEmail(),
                "Recuperación de contraseña - Elevideo",
                "emails/password-reset",
                variables
        );
    }

    // ==================== MÉTODOS PRIVADOS - URL BUILDERS ====================

    /**
     * Construye la URL de verificación de email con el token.
     */
    private String buildVerificationUrl(String token) {
        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path(verifyEmailPath)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    /**
     * Construye la URL de reseteo de contraseña con el token.
     */
    private String buildPasswordResetUrl(String token) {
        return UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path(resetPasswordPath)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

}
