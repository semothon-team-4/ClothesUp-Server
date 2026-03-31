package semothon.team4.clothesup.user.dto.authdto;

import java.util.List;
import lombok.Data;
import semothon.team4.clothesup.user.domain.User;

@Data

public class SignUpRequest {

    String email;
    String password;
    String nickname;

    public User toUser(String encodedPassword) {
        return new User(email, encodedPassword, List.of(User.Role.USER), nickname);
    }
}


