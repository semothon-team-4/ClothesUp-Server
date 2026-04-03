package semothon.team4.clothesup.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.user.domain.User;

@Entity
@Getter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {jakarta.persistence.CascadeType.MERGE})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Analysis analysis;

    private String title;

    @Lob
    private String content;

    private boolean isPublic;

    private LocalDateTime createdAt;

    public Post(User user, Analysis analysis, String title, String content, boolean isPublic) {
        this.user = user;
        this.analysis = analysis;
        this.title = title;
        this.content = content;
        this.isPublic = isPublic;
        this.createdAt = LocalDateTime.now();
    }
}
