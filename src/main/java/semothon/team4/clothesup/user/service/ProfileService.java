package semothon.team4.clothesup.user.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import semothon.team4.clothesup.global.common.S3Service;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.CommonErrorCode;
import semothon.team4.clothesup.global.exception.code.UserErrorCode;
import semothon.team4.clothesup.shop.domain.SavedShop;
import semothon.team4.clothesup.shop.domain.Shop;
import semothon.team4.clothesup.shop.repository.ReviewImageRepository;
import semothon.team4.clothesup.shop.repository.ReviewRepository;
import semothon.team4.clothesup.shop.repository.SavedShopRepository;
import semothon.team4.clothesup.shop.repository.ShopRepository;
import semothon.team4.clothesup.user.domain.Post;
import semothon.team4.clothesup.user.domain.User;
import semothon.team4.clothesup.user.dto.postdto.PostListResponse;
import semothon.team4.clothesup.user.dto.profiledto.ReviewResponse;
import semothon.team4.clothesup.user.dto.profiledto.SavedShopResponse;
import semothon.team4.clothesup.user.dto.profiledto.UserProfileResponse;
import semothon.team4.clothesup.user.repository.CommentRepository;
import semothon.team4.clothesup.user.repository.PostLikeRepository;
import semothon.team4.clothesup.user.repository.PostRepository;
import semothon.team4.clothesup.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final SavedShopRepository savedShopRepository;
    private final PostLikeRepository postLikeRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ShopRepository shopRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;

    public UserProfileResponse getMyProfile(User user) {
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(UserErrorCode.USER_NOT_FOUND));

        long savedShopCount = savedShopRepository.countByUser(persistentUser);
        long likedPostCount = postLikeRepository.countByUser(persistentUser);
        long reviewCount = reviewRepository.countByUser(persistentUser);

        return UserProfileResponse.builder()
            .nickname(persistentUser.getNickname())
            .profileImage(s3Service.getPresignedUrl(persistentUser.getProfileImage()))
            .savedShopCount(savedShopCount)
            .likedPostCount(likedPostCount)
            .reviewCount(reviewCount)
            .build();
    }

    public List<SavedShopResponse> getSavedShops(User user) {
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(UserErrorCode.USER_NOT_FOUND));

        return savedShopRepository.findAllByUserOrderByCreatedAtDesc(persistentUser).stream()
            .map(savedShop -> {
                Shop shop = savedShop.getShop();
                return SavedShopResponse.builder()
                    .id(shop.getId())
                    .name(shop.getName())
                    .imageUrl(s3Service.getPresignedUrl(shop.getImageUrl()))
                    .openTime(shop.getOpenTime() != null ? shop.getOpenTime().toString() : null)
                    .closeTime(shop.getCloseTime() != null ? shop.getCloseTime().toString() : null)
                    .rate(shop.getRate())
                    .likeCount(shop.getLike())
                    .build();
            })
            .collect(Collectors.toList());
    }

    // 좋아요 누른 글 목록 조회
    public List<PostListResponse> getLikedPosts(User user) {
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(UserErrorCode.USER_NOT_FOUND));

        return postLikeRepository.findAllByUserOrderByCreatedAtDesc(persistentUser).stream()
            .map(postLike -> convertToPostListResponse(postLike.getPost(), persistentUser))
            .collect(Collectors.toList());
    }

    // 내 리뷰 내역 조회
    public List<ReviewResponse> getMyReviews(User user) {
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(UserErrorCode.USER_NOT_FOUND));

        return reviewRepository.findAllByUserOrderByCreatedAtDesc(persistentUser).stream()
            .map(review -> {
                List<String> imageUrls = reviewImageRepository.findByReview(review).stream()
                    .map(img -> s3Service.getPresignedUrl(img.getImageUrl()))
                    .collect(Collectors.toList());

                return ReviewResponse.builder()
                    .id(review.getId())
                    .shopId(review.getShop().getId())
                    .shopName(review.getShop().getName())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .imageUrls(imageUrls)
                    .isVerified(review.getReceipt() != null) // 영수증이 존재하면 인증됨
                    .createdAt(review.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public boolean toggleSaveShop(Long shopId, User user) {
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(UserErrorCode.USER_NOT_FOUND));

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new CoreException(CommonErrorCode.RESOURCE_NOT_FOUND));

        return savedShopRepository.findByUserAndShop(persistentUser, shop)
            .map(savedShop -> {
                savedShopRepository.delete(savedShop);
                shop.updateLikeCount(-1); // 좋아요 수 감소
                return false;
            })
            .orElseGet(() -> {
                savedShopRepository.save(new SavedShop(persistentUser, shop));
                shop.updateLikeCount(1); // 좋아요 수 증가
                return true;
            });
    }

    private PostListResponse convertToPostListResponse(Post post, User user) {
        long commentCount = commentRepository.countByPost(post);
        boolean isLiked = postLikeRepository.existsByPostAndUser(post, user);
        long likeCount = postLikeRepository.countByPost(post);

        String summaryContent = post.getContent();
        if (summaryContent != null && summaryContent.length() > 100) {
            summaryContent = summaryContent.substring(0, 100) + "... 더보기";
        }

        return PostListResponse.builder()
            .id(post.getId())
            .category(post.getCategory())
            .title(post.getTitle())
            .content(summaryContent)
            .authorNickname(post.getUser().getNickname())
            .authorProfileImage(s3Service.getPresignedUrl(post.getUser().getProfileImage()))
            .imageUrl(post.getAnalysis() != null
                ? s3Service.getPresignedUrl(post.getAnalysis().getImageUrl())
                : post.getImageUrl() != null ? s3Service.getPresignedUrl(post.getImageUrl()) : null)
            .analysisName(post.getAnalysis() != null ? post.getAnalysis().getName() : null)
            .likeCount(likeCount)
            .commentCount(commentCount)
            .isLiked(isLiked)
            .createdAt(post.getCreatedAt())
            .build();
    }
}
