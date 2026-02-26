package com.elevideo.backend.service;

import com.elevideo.backend.dto.user.AuthenticatedUserResponse;
import com.elevideo.backend.dto.user.ChangePasswordRequest;
import com.elevideo.backend.dto.user.UserRes;
import com.elevideo.backend.dto.user.UserUpdateRequest;
import com.elevideo.backend.exception.SamePasswordException;
import com.elevideo.backend.exception.UserNotFoundException;
import com.elevideo.backend.mapper.UserMapper;
import com.elevideo.backend.model.User;
import com.elevideo.backend.repository.UserRepository;
import com.elevideo.backend.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final CurrentUserProvider currentUserProvider;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserRes getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        return userMapper.toUserRes(user);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
    public AuthenticatedUserResponse getAuthUser() {

        UUID userId = currentUserProvider.getCurrentUserId();
        User user =  userRepository.findById(userId).orElseThrow(()->new UserNotFoundException("Usuario no encontrado"));
        return userMapper.toAuthenticatedUserResponse(user);

    }

    public AuthenticatedUserResponse updateUser(UserUpdateRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();
        User user =  userRepository.findById(userId).orElseThrow(()->new UserNotFoundException("Usuario no encontrado"));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);
        return userMapper.toAuthenticatedUserResponse(user);

    }

    public void changePassword(UUID id, ChangePasswordRequest request) {

        UUID userId = currentUserProvider.getCurrentUserId();
        User user =  userRepository.findById(userId).orElseThrow(()->new UserNotFoundException("Usuario no encontrado"));

        // Validar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new SamePasswordException("Contraseña actual inválida");
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
