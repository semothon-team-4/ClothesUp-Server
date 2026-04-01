package semothon.team4.clothesup.analysis.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.analysis.dto.AnalysisDetailResponse;
import semothon.team4.clothesup.analysis.dto.AnalysisListItemResponse;
import semothon.team4.clothesup.analysis.service.AnalysisService;
import semothon.team4.clothesup.global.common.BaseResponse;
import semothon.team4.clothesup.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analyses")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Operation(summary = "옷 분석 요청")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<AnalysisDetailResponse>> requestAnalysis(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam String name,
        @RequestParam String category,
        @RequestParam MultipartFile image
    ) {
        return BaseResponse.created("분석 요청 성공",
            analysisService.requestAnalysis(userDetails.getUser(), name, category, image));
    }

    @Operation(summary = "내 옷장 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<List<AnalysisListItemResponse>>> getMyAnalyses(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return BaseResponse.ok("옷장 목록 조회 성공",
            analysisService.getMyAnalyses(userDetails.getUser()));
    }

    @Operation(summary = "분석 결과 상세 조회")
    @GetMapping("/{analysisId}")
    public ResponseEntity<BaseResponse<AnalysisDetailResponse>> getAnalysisDetail(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long analysisId
    ) {
        return BaseResponse.ok("분석 결과 조회 성공",
            analysisService.getAnalysisDetail(userDetails.getUser(), analysisId));
    }

    @Operation(summary = "분석 항목 삭제")
    @DeleteMapping("/{analysisId}")
    public ResponseEntity<BaseResponse<Void>> deleteAnalysis(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long analysisId
    ) {
        analysisService.deleteAnalysis(userDetails.getUser(), analysisId);
        return BaseResponse.ok("분석 항목 삭제 성공", null);
    }
}
