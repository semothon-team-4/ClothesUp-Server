package semothon.team4.clothesup.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.global.common.BaseResponse;
import semothon.team4.clothesup.global.security.CustomUserDetails;
import semothon.team4.clothesup.shop.dto.ReceiptDetailResponse;
import semothon.team4.clothesup.shop.dto.ReceiptUploadResponse;
import semothon.team4.clothesup.shop.service.ReceiptService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    @Operation(summary = "영수증 업로드")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ReceiptUploadResponse>> uploadReceipt(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam Long shopId,
        @RequestParam MultipartFile image
    ) {
        return BaseResponse.created("영수증 업로드 성공",
            receiptService.uploadReceipt(userDetails.getUser(), shopId, image));
    }

    @Operation(summary = "영수증 상세 조회")
    @GetMapping("/{receiptId}")
    public ResponseEntity<BaseResponse<ReceiptDetailResponse>> getReceipt(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long receiptId
    ) {
        return BaseResponse.ok("영수증 조회 성공",
            receiptService.getReceipt(userDetails.getUser(), receiptId));
    }
}
