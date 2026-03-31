package semothon.team4.clothesup.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    private String photo;

    @Column(name = "encrypt_pwd", nullable = false)
    private String encryptPwd;

    private String nickname;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Role> roles = new ArrayList<>();

    public User(String email, String encodedPassword, List<Role> user, String nickname) {
        this.email = email;
        this.encryptPwd = encodedPassword;
        this.roles = user;
        this.nickname = nickname;
    }

    public enum Role {
        ADMIN, USER
    }

}
