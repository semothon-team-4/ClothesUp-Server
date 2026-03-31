package semothon.team4.clothesup.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import semothon.team4.clothesup.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTH_REQUIRED(40101, HttpStatus.UNAUTHORIZED, "Authentication required."),
    INVALID_TOKEN(40102, HttpStatus.UNAUTHORIZED, "Invalid token."),
    EXPIRED_TOKEN(40103, HttpStatus.UNAUTHORIZED, "Token has expired."),
    UNAUTHORIZED(40104, HttpStatus.UNAUTHORIZED, "Unauthorized access."),
    INVALID_CREDENTIALS(40105, HttpStatus.UNAUTHORIZED, "Invalid username or password."),

    FORBIDDEN(40301, HttpStatus.FORBIDDEN, "Access denied."),
    INSUFFICIENT_PRIVILEGES(40302, HttpStatus.FORBIDDEN, "Insufficient privileges.");

    private final int code;
    private final HttpStatus status;
    private final String message;
}

