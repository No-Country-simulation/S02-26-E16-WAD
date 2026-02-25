package com.elevideo.backend.controller;

import com.elevideo.backend.dto.ApiResult;
import com.elevideo.backend.dto.user.AuthenticatedUserResponse;
import com.elevideo.backend.dto.user.UserRes;
import com.elevideo.backend.dto.user.UserUpdateRequest;
import com.elevideo.backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")

public class UserController {

    private final UserService userService;
    private Object me;

    @GetMapping("/me")
    public ResponseEntity <?> getAuthUser() {
         AuthenticatedUserResponse response = userService.getAuthUser();

         return ResponseEntity.ok(ApiResult.success(response,"Datos obtenidos correctamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResult<UserRes>> getUserById(@PathVariable UUID id) {
        UserRes response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResult.success(response, "Usuario obtenido correctamente"));
    }


    @PatchMapping
    public ResponseEntity<ApiResult<AuthenticatedUserResponse>> updateUser(@RequestBody UserUpdateRequest request) {
       AuthenticatedUserResponse response = userService.updateUser(request);
        return ResponseEntity.ok(ApiResult.success(response,"Datos actuallizados correctamente"));

    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }
}
