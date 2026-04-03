package semothon.team4.clothesup.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {
    LAUNDRY_TIP("세탁팁"),
    REPAIR("수선"),
    RECOMMEND("제품추천"),
    CONDITION("의류상태");

    private final String description;
}
