package semothon.team4.clothesup.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.global.common.BaseResponse;
import semothon.team4.clothesup.global.security.CustomUserDetails;
import semothon.team4.clothesup.user.dto.UserProfileResponse;
import semothon.team4.clothesup.user.dto.UserProfileUpdateResponse;
import semothon.team4.clothesup.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserProfileResponse>> getMyProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return BaseResponse.ok("프로필 조회 성공",
            userService.getMyProfile(userDetails.getUser()));
    }

    @Operation(summary = "내 프로필 수정")
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<UserProfileUpdateResponse>> updateMyProfile(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) String nickname,
        @RequestParam(required = false) MultipartFile profileImage
    ) {
        return BaseResponse.ok("프로필 수정 성공",
            userService.updateMyProfile(userDetails.getUser(), nickname, profileImage));
    }
}
