package semothon.team4.clothesup.user.dto.authdto;


import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import semothon.team4.clothesup.user.domain.User.Role;

@Data
@AllArgsConstructor
public class SignUpResponse {
    String email;
    String nickname;
    List<Role> roles;
}
