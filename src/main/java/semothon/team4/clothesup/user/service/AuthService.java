package semothon.team4.clothesup.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.UserErrorCode;
import semothon.team4.clothesup.global.security.JwtToken;
import semothon.team4.clothesup.global.security.JwtTokenProvider;
import semothon.team4.clothesup.user.domain.User;
import semothon.team4.clothesup.user.dto.authdto.LoginResponse;
import semothon.team4.clothesup.user.dto.authdto.SignUpRequest;
import semothon.team4.clothesup.user.dto.authdto.SignUpResponse;
import semothon.team4.clothesup.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userAuthRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(String email, String password) {
        log.info("AuthenticationToken 생성: email={}", email);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            email, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        log.info("Authentication 성공: email={}", email);
        JwtToken token = jwtTokenProvider.generateToken(authentication);
        log.info("JWT 토큰 생성 완료: email={}", email);

        User user = userAuthRepository.findByEmail(email)
            .orElseThrow(() -> new CoreException(UserErrorCode.USER_NOT_FOUND));

        return new LoginResponse(token.getGrantType(), token.getAccessToken(),
            token.getRefreshToken());
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest signUpRequest) {

        if (userAuthRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new CoreException(UserErrorCode.USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());
        User user = userAuthRepository.save(signUpRequest.toUser(encodedPassword));
        return new SignUpResponse(user.getEmail(), user.getNickname(), user.getRoles());
    }
}
