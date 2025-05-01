package com.contract.backend.service;

import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.LoginRequestDTO;
import com.contract.backend.common.dto.SignupRequestDTO;
import com.contract.backend.common.dto.UserResponseDTO;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.uitl.jwt.JwtTokenProvider;
import com.contract.backend.common.repository.UserRepository;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UserResponseDTO signup(SignupRequestDTO request){
        Optional<UserEntity> existing = userRepository.findByEmail(request.getEmail());
        if(existing.isPresent()){
            throw new CustomException(CustomExceptionEnum.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        UserEntity user = new UserEntity(request.getUsername(), request.getEmail(), encodedPassword);
        UserEntity savedUser = userRepository.save(user);

        return new UserResponseDTO(savedUser.getId(), savedUser.getUserName(), savedUser.getEmail());
    }

    public String  login(LoginRequestDTO request){
        UserEntity user =  userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.EMAIL_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new CustomException(CustomExceptionEnum.PASSWORD_MISMATCH);
        }

        return jwtTokenProvider.createToken(user.getUuid());
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.EMAIL_NOT_FOUND));
    }

    public UserEntity findByUuid(String uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.EMAIL_NOT_FOUND));
    }

}
