package com.backend.bilanko.controller.person;

import com.backend.bilanko.DTO.person.UserResponseDTO;
import com.backend.bilanko.mapper.UserMapper;
import com.backend.bilanko.services.person.UserServices;
import com.backend.bilanko.utils.routes.UserApiRoutes;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserApiRoutes.USERS)
@AllArgsConstructor
public class UserController {
    private final UserServices userServices;
    @GetMapping(UserApiRoutes.GET_USER)
    public ResponseEntity<UserResponseDTO> getCurentUser(Authentication authentication){
        return ResponseEntity.ok(UserMapper.toUserResponseDto(userServices.findUserByEmail(authentication.getName())));
    }
}
