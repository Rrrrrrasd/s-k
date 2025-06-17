package com.contract.backend.service;

import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.AuthResponseDTO;
import com.contract.backend.common.dto.LoginRequestDTO;
import com.contract.backend.common.dto.SignupRequestDTO;
import com.contract.backend.common.dto.UserResponseDTO;
import com.contract.backend.common.exception.CustomException;
import com.contract.backend.common.exception.CustomExceptionEnum;
import com.contract.backend.common.util.jwt.JwtTokenProvider;
import com.contract.backend.common.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public AuthResponseDTO login(LoginRequestDTO request){
        UserEntity user =  userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.EMAIL_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new CustomException(CustomExceptionEnum.PASSWORD_MISMATCH);
        }

        String uuid = user.getUuid();
        String accessToken  = jwtTokenProvider.createToken(uuid);
        String refreshToken = jwtTokenProvider.createRefreshToken(uuid);
        return new AuthResponseDTO(accessToken, refreshToken);
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.EMAIL_NOT_FOUND));
    }

    public UserEntity findByUuid(String uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new CustomException(CustomExceptionEnum.EMAIL_NOT_FOUND));
    }

    public void logout(HttpServletResponse response) {
        // 빈 값 + maxAge=0 인 쿠키를 내려서 브라우저가 지우게 함
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)       // 운영 시 true, dev 환경이면 false
                .path("/")
                .maxAge(0)          // 즉시 만료
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        // stateless 환경에서도 혹시 남아있을 수 있는 컨텍스트 삭제
        SecurityContextHolder.clearContext();
    }
}
