package semothon.team4.clothesup.shop.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Receipt;

@Getter
@Builder
public class ReceiptUploadResponse {
    private Long id;
    private Long shopId;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static ReceiptUploadResponse from(Receipt receipt) {
        return ReceiptUploadResponse.builder()
            .id(receipt.getId())
            .shopId(receipt.getShop().getId())
            .imageUrl(receipt.getImageUrl())
            .createdAt(receipt.getCreatedAt())
            .build();
    }
}
