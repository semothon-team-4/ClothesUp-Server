package semothon.team4.clothesup.user.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.global.s3.S3Uploader;
import semothon.team4.clothesup.user.domain.User;
import semothon.team4.clothesup.user.dto.UserProfileResponse;
import semothon.team4.clothesup.user.dto.UserProfileUpdateResponse;
import semothon.team4.clothesup.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    public UserProfileResponse getMyProfile(User user) {
        return UserProfileResponse.from(user, toPresignedUrl(user.getProfileImage()));
    }

    @Transactional
    public UserProfileUpdateResponse updateMyProfile(User user, String nickname, MultipartFile profileImage) {
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname);
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            if (user.getProfileImage() != null) {
                s3Uploader.delete(user.getProfileImage());
            }
            String key = s3Uploader.upload(profileImage, "profiles");
            user.setProfileImage(key);
        }

        userRepository.save(user);
        return UserProfileUpdateResponse.from(user, toPresignedUrl(user.getProfileImage()));
    }

    private String toPresignedUrl(String key) {
        if (key == null) return null;
        return s3Uploader.generatePresignedUrl(key, PRESIGNED_URL_EXPIRATION);
    }
}
