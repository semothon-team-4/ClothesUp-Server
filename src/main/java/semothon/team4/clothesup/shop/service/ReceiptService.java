package semothon.team4.clothesup.shop.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.ReceiptErrorCode;
import semothon.team4.clothesup.global.exception.code.ShopErrorCode;
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

    private final ReceiptRepository receiptRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public ReceiptUploadResponse uploadReceipt(User user, Long shopId, MultipartFile image) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(ShopErrorCode.SHOP_NOT_FOUND));

        // TODO: 실제 파일 스토리지(S3 등) 연동 시 아래 imageUrl을 교체하세요
        String imageUrl = image.getOriginalFilename();

        Receipt receipt = Receipt.builder()
            .user(user)
            .shop(shop)
            .imageUrl(imageUrl)
            .createdAt(LocalDateTime.now())
            .build();

        return ReceiptUploadResponse.from(receiptRepository.save(receipt));
    }

    public ReceiptDetailResponse getReceipt(User user, Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new CoreException(ReceiptErrorCode.RECEIPT_NOT_FOUND));

        if (!receipt.getUser().getId().equals(user.getId())) {
            throw new CoreException(ReceiptErrorCode.RECEIPT_ACCESS_DENIED);
        }

        return ReceiptDetailResponse.from(receipt);
    }
}
