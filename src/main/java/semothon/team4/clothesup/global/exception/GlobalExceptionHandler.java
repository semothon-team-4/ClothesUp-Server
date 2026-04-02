package semothon.team4.clothesup.global.exception;


import io.jsonwebtoken.ExpiredJwtException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import semothon.team4.clothesup.global.common.BaseResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CoreException 처리
     */
    @ExceptionHandler(CoreException.class)
    public ResponseEntity<BaseResponse<?>> handleCoreException(CoreException e) {
        ErrorCode code = e.getErrorCode();
        log.error("[CoreException] {}: {}", code.getCode(), code.getMessage(), e);

        return BaseResponse.of(code.getStatus(), code.getMessage());
    }

    /**
     * @Valid 검증 실패 시
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
            .getAllErrors()
            .stream()
            .map(err -> {
                if (err instanceof FieldError fieldError) {
                    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                } else {
                    return err.getDefaultMessage();
                }
            })
            .collect(Collectors.joining(", "));

        log.warn("[ValidationException] {}", errorMessage);

        return BaseResponse.of(HttpStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * Spring Security 예외
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<?>> handleBadCredentials() {
        return BaseResponse.of(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<?>> handleAccessDenied() {
        return BaseResponse.of(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<BaseResponse<?>> handleExpiredJwt() {
        return BaseResponse.of(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해주세요.");
    }

    // 404 Not Found
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleNotFound(NoResourceFoundException ex) {
        log.error("[NotFoundException] {}", ex.getMessage(), ex);
        return BaseResponse.of(HttpStatus.NOT_FOUND, "요청한 URL을 찾을 수 없습니다.");
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        log.error("[MethodNotSupportedException] {}", ex.getMessage());
        return BaseResponse.of(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.");
    }

    /**
     * 모든 예외를 잡는 최후의 보루
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<BaseResponse<?>> handleThrowable(Throwable e) {
        log.error("[Fatal Error] 예상치 못한 심각한 오류 발생: {}", e.getMessage(), e);
        return BaseResponse.internalServerError("서버 내부 오류가 발생했습니다.");
    }


    /**
     * 그 외 알 수 없는 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);

        return BaseResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}

