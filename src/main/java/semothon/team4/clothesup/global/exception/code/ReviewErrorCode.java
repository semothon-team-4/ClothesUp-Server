package semothon.team4.clothesup.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import semothon.team4.clothesup.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "Review not found."),
    REVIEW_ACCESS_DENIED(40312, HttpStatus.FORBIDDEN, "No permission to delete this review.");

    private final int code;
    private final HttpStatus status;
    private final String message;
}
