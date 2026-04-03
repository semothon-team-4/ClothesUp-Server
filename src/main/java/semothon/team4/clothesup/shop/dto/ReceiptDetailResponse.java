package semothon.team4.clothesup.shop.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import semothon.team4.clothesup.shop.domain.Receipt;

@Getter
@Builder
public class ReceiptDetailResponse {
    private Long id;
    private Long shopId;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static ReceiptDetailResponse from(Receipt receipt, String imageUrl) {
        return ReceiptDetailResponse.builder()
            .id(receipt.getId())
            .shopId(receipt.getShop().getId())
            .imageUrl(imageUrl)
            .createdAt(receipt.getCreatedAt())
            .build();
    }
}
