package semothon.team4.clothesup.user.dto.authdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}