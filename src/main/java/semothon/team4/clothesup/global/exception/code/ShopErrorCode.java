package semothon.team4.clothesup.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import semothon.team4.clothesup.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ShopErrorCode implements ErrorCode {

    SHOP_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "Shop not found.");

    private final int code;
    private final HttpStatus status;
    private final String message;
}
