package semothon.team4.clothesup.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import semothon.team4.clothesup.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ReceiptErrorCode implements ErrorCode {

    RECEIPT_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "Receipt not found."),
    RECEIPT_ACCESS_DENIED(40311, HttpStatus.FORBIDDEN, "No permission to access this receipt.");

    private final int code;
    private final HttpStatus status;
    private final String message;
}
