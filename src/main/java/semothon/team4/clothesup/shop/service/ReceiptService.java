package semothon.team4.clothesup.shop.service;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.ReceiptErrorCode;
import semothon.team4.clothesup.global.exception.code.ShopErrorCode;
import semothon.team4.clothesup.global.s3.S3Uploader;
import semothon.team4.clothesup.shop.domain.Receipt;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.shop.dto.ReceiptDetailResponse;
import semothon.team4.clothesup.shop.dto.ReceiptUploadResponse;
import semothon.team4.clothesup.shop.repository.ReceiptRepository;
import semothon.team4.clothesup.shop.repository.ShopRepository;
import semothon.team4.clothesup.user.domain.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptService {

    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofHours(1);

    private final ReceiptRepository receiptRepository;
    private final ShopRepository shopRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public ReceiptUploadResponse uploadReceipt(User user, Long shopId, MultipartFile image) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));

        String key = s3Uploader.upload(image, "receipts");

        Receipt receipt = receiptRepository.save(Receipt.builder()
            .user(user)
            .shop(shop)
            .imageUrl(key)
            .createdAt(LocalDateTime.now())
            .build());

        String presignedUrl = s3Uploader.generatePresignedUrl(key, PRESIGNED_URL_EXPIRATION);
        return ReceiptUploadResponse.from(receipt, presignedUrl);
    }

    public ReceiptDetailResponse getReceipt(User user, Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new CoreException(ReceiptErrorCode.RECEIPT_NOT_FOUND));

        if (!receipt.getUser().getId().equals(user.getId())) {
            throw new CoreException(ReceiptErrorCode.RECEIPT_ACCESS_DENIED);
        }

        String presignedUrl = s3Uploader.generatePresignedUrl(receipt.getImageUrl(), PRESIGNED_URL_EXPIRATION);
        return ReceiptDetailResponse.from(receipt, presignedUrl);
    }
}
