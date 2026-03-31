package semothon.team4.clothesup.user.dto.authdto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
