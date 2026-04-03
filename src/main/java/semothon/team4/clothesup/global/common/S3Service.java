package semothon.team4.clothesup.global.common;

import org.springframework.stereotype.Service;

@Service
public class S3Service {

    /**
     * S3 객체의 Pre-signed URL을 생성합니다.
     * 현재는 실제 S3 연동 전이므로, 전달받은 경로를 그대로 반환하거나
     * 로컬 테스트를 위한 URL 형식을 반환합니다.
     */
    public String getPresignedUrl(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        // TODO: 실제 S3 연동 시 AWS SDK를 사용하여 Pre-signed URL 생성 로직을 구현하세요.
        // 임시로 path를 그대로 반환하거나 prefix를 붙여 반환할 수 있습니다.
        return path;
    }
}
