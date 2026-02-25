package com.elevideo.backend.mapper;


import com.elevideo.backend.dto.JwtDataDto;
import com.elevideo.backend.dto.auth.RegisterReq;
import com.elevideo.backend.dto.user.AuthenticatedUserResponse;
import com.elevideo.backend.dto.user.UserRes;
import com.elevideo.backend.enums.AccountStatus;
import com.elevideo.backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",  imports = {AccountStatus.class})
public interface UserMapper {

    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "email", expression = "java(request.email() == null ? null : request.email().toLowerCase())")
    @Mapping(target = "accountStatus", expression = "java(AccountStatus.ACTIVE)")
    @Mapping(target = "emailVerified", constant = "false")
    User toUser(RegisterReq request, String encodedPassword);

    UserRes toUserRes(User user);

    @Mapping(target = "id", source = "id")
    JwtDataDto toJwtDataDto(User user);

    AuthenticatedUserResponse toAuthenticatedUserResponse(User user);


}
