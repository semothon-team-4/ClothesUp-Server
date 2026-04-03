package semothon.team4.clothesup.global.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import semothon.team4.clothesup.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AnalysisErrorCode implements ErrorCode {

    ANALYSIS_NOT_FOUND(40404, HttpStatus.NOT_FOUND, "Analysis not found."),
    ANALYSIS_ACCESS_DENIED(40313, HttpStatus.FORBIDDEN, "No permission to access this analysis."),
    CARE_LABEL_NOT_FOUND(40405, HttpStatus.NOT_FOUND, "Care label symbol not found.");

    private final int code;
    private final HttpStatus status;
    private final String message;
}
