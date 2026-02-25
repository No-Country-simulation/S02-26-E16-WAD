package com.elevideo.backend.service;

import com.elevideo.backend.dto.user.AuthenticatedUserResponse;
import com.elevideo.backend.dto.user.UserRes;
import com.elevideo.backend.dto.user.UserUpdateRequest;
import com.elevideo.backend.exception.UserNotFoundException;
import com.elevideo.backend.mapper.UserMapper;
import com.elevideo.backend.model.User;
import com.elevideo.backend.repository.UserRepository;
import com.elevideo.backend.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final CurrentUserProvider currentUserProvider;

    private final UserMapper userMapper;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserRes getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        return userMapper.toUserRes(user);
    }


    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(UUID id, User updatedUser) {
        UserRes user = getUserById(id);

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setEmail(updatedUser.getEmail());
        user.setAccountStatus(updatedUser.getAccountStatus());

        return userRepository.save(user);
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
}
