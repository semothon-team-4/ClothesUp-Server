package semothon.team4.clothesup.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import semothon.team4.clothesup.global.common.BaseResponse;
import semothon.team4.clothesup.user.dto.authdto.LoginRequest;
import semothon.team4.clothesup.user.dto.authdto.LoginResponse;
import semothon.team4.clothesup.user.dto.authdto.SignUpRequest;
import semothon.team4.clothesup.user.dto.authdto.SignUpResponse;
import semothon.team4.clothesup.user.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자 로그인")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 일치하지 않습니다.")
    })
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest){
        LoginResponse response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return BaseResponse.ok("로그인 성공", response);
    }

    @Operation(summary = "회원가입", description = "사용자 회원가입")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 회원입니다")
    })
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest signUpRequest){
        SignUpResponse signUpResponse = authService.signUp(signUpRequest);
        return BaseResponse.created("회원가입", signUpResponse);
    }
}
