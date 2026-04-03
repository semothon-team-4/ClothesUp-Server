package semothon.team4.clothesup.user.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.analysis.repository.AnalysisRepository;
import semothon.team4.clothesup.global.common.S3Service;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.s3.S3Uploader;
import semothon.team4.clothesup.global.exception.code.CommonErrorCode;
import semothon.team4.clothesup.user.domain.Comment;
import semothon.team4.clothesup.user.domain.Post;
import semothon.team4.clothesup.user.domain.PostCategory;
import semothon.team4.clothesup.user.domain.PostLike;
import semothon.team4.clothesup.user.dto.postdto.*;
import semothon.team4.clothesup.user.repository.CommentRepository;
import semothon.team4.clothesup.user.repository.PostLikeRepository;
import semothon.team4.clothesup.user.repository.PostRepository;
import semothon.team4.clothesup.user.domain.User;
import semothon.team4.clothesup.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final S3Uploader s3Uploader;

    @Transactional
    public Long createPost(PostCreateRequest request, MultipartFile image, User user) {
        if (user == null || user.getId() == null) {
            throw new CoreException(semothon.team4.clothesup.global.exception.code.UserErrorCode.USER_NOT_FOUND);
        }
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(semothon.team4.clothesup.global.exception.code.UserErrorCode.USER_NOT_FOUND));

        Post post;
        if (request.getAnalysisId() != null) {
            Analysis analysis = analysisRepository.findById(request.getAnalysisId())
                .orElseThrow(() -> new CoreException(CommonErrorCode.RESOURCE_NOT_FOUND));
            post = new Post(persistentUser, analysis, request.getTitle(), request.getContent(), request.isPublic(), request.getCategory());
        } else if (image != null && !image.isEmpty()) {
            String imageKey = s3Uploader.upload(image, "posts");
            post = new Post(persistentUser, imageKey, request.getTitle(), request.getContent(), request.isPublic(), request.getCategory());
        } else {
            post = new Post(persistentUser, (Analysis) null, request.getTitle(), request.getContent(), request.isPublic(), request.getCategory());
        }

        return postRepository.save(post).getId();
    }

    // [목록 조회] RECOMMENDED, LATEST, POPULAR 지원
    public List<PostListResponse> getPosts(User user, String sort, PostCategory category) {
        User persistentUser = user != null ? userRepository.findById(user.getId()).orElse(null) : null;
        
        List<Post> posts;
        if (category == null) {
            posts = postRepository.findAll();
        } else {
            posts = postRepository.findAllByCategoryOrderByCreatedAtDesc(category);
        }

        // 정렬 처리
        if ("POPULAR".equalsIgnoreCase(sort)) {
            // 인기순 (단순 좋아요 순)
            return posts.stream()
                .sorted((p1, p2) -> Long.compare(postLikeRepository.countByPost(p2), postLikeRepository.countByPost(p1)))
                .map(post -> convertToPostListResponse(post, persistentUser))
                .collect(Collectors.toList());
        } else if ("RECOMMENDED".equalsIgnoreCase(sort)) {
            // 추천순 (가중치 + 시간 감쇄 알고리즘)
            return posts.stream()
                .sorted((p1, p2) -> Double.compare(calculateRecommendationScore(p2), calculateRecommendationScore(p1)))
                .map(post -> convertToPostListResponse(post, persistentUser))
                .collect(Collectors.toList());
        } else {
            // 최신순 (기본값)
            return posts.stream()
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .map(post -> convertToPostListResponse(post, persistentUser))
                .collect(Collectors.toList());
        }
    }

    // 추천 점수 계산 (Time Decay 알고리즘)
    private double calculateRecommendationScore(Post post) {
        long likes = postLikeRepository.countByPost(post);
        long comments = commentRepository.countByPost(post);
        
        // (좋아요*3 + 댓글*5 + 기본점수1)
        double weightScore = (likes * 3.0) + (comments * 5.0) + 1.0;
        
        // 작성 후 경과 시간(시간 단위)
        long hoursSince = ChronoUnit.HOURS.between(post.getCreatedAt(), LocalDateTime.now());
        
        // 점수 = 가중치 점수 / (경과시간 + 2)^1.8
        return weightScore / Math.pow(hoursSince + 2.0, 1.8);
    }

    // [실시간 인기글] 최근 24시간 내 게시글 중 좋아요 순 (최대 5개)
    public List<PostListResponse> getPopularPosts(User user, PostCategory category) {
        User persistentUser = user != null ? userRepository.findById(user.getId()).orElse(null) : null;
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        
        List<Post> posts;
        if (category == null) {
            posts = postRepository.findPopularPostsSince(twentyFourHoursAgo);
        } else {
            posts = postRepository.findPopularPostsByCategorySince(category, twentyFourHoursAgo);
        }

        return posts.stream()
            .limit(5)
            .map(post -> convertToPostListResponse(post, persistentUser))
            .collect(Collectors.toList());
    }

    // [검색 기능] 키워드로 게시글 검색
    public List<PostListResponse> searchPosts(User user, String keyword, PostCategory category) {
        User persistentUser = user != null ? userRepository.findById(user.getId()).orElse(null) : null;
        
        List<Post> posts;
        if (category == null) {
            posts = postRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(keyword, keyword);
        } else {
            posts = postRepository.findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByCreatedAtDesc(category, keyword, category, keyword);
        }

        return posts.stream()
            .map(post -> convertToPostListResponse(post, persistentUser))
            .collect(Collectors.toList());
    }

    // [상세 조회] 모든 정보와 댓글 목록 포함
    public PostResponse getPostDetail(Long postId, User user) {
        User persistentUser = user != null ? userRepository.findById(user.getId()).orElse(null) : null;
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CoreException(CommonErrorCode.RESOURCE_NOT_FOUND));
        return convertToPostResponse(post, persistentUser);
    }

    @Transactional
    public void addComment(Long postId, CommentRequest request, User user) {
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(semothon.team4.clothesup.global.exception.code.UserErrorCode.USER_NOT_FOUND));
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CoreException(CommonErrorCode.RESOURCE_NOT_FOUND));
        Comment comment = new Comment(post, persistentUser, request.getContent());
        commentRepository.save(comment);
    }

    @Transactional
    public boolean toggleLike(Long postId, User user) {
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(semothon.team4.clothesup.global.exception.code.UserErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CoreException(CommonErrorCode.RESOURCE_NOT_FOUND));
        
        return postLikeRepository.findByPostAndUser(post, persistentUser)
            .map(like -> {
                postLikeRepository.delete(like);
                return false;
            })
            .orElseGet(() -> {
                postLikeRepository.save(new PostLike(post, persistentUser));
                return true;
            });
    }

    // 목록용 변환 (본문 100자 제한 + 댓글 리스트 제외)
    private PostListResponse convertToPostListResponse(Post post, User user) {
        long commentCount = commentRepository.countByPost(post);
        boolean isLiked = user != null && postLikeRepository.existsByPostAndUser(post, user);
        long likeCount = postLikeRepository.countByPost(post);

        // 본문 100자 제한 로직
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
            .analysisImageUrl(post.getAnalysis() != null ? s3Service.getPresignedUrl(post.getAnalysis().getImageUrl()) : null)
            .analysisName(post.getAnalysis() != null ? post.getAnalysis().getName() : null)
            .postImageUrl(post.getImageUrl() != null ? s3Service.getPresignedUrl(post.getImageUrl()) : null)
            .likeCount(likeCount)
            .commentCount(commentCount)
            .isLiked(isLiked)
            .createdAt(post.getCreatedAt())
            .build();
    }

    // 상세용 변환 (본문 전체 + 댓글 리스트 포함)
    private PostResponse convertToPostResponse(Post post, User user) {
        List<CommentResponse> comments = commentRepository.findAllByPost(post).stream()
            .map(comment -> CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorNickname(comment.getUser().getNickname())
                .authorProfileImage(s3Service.getPresignedUrl(comment.getUser().getProfileImage()))
                .createdAt(comment.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        boolean isLiked = user != null && postLikeRepository.existsByPostAndUser(post, user);
        long likeCount = postLikeRepository.countByPost(post);

        return PostResponse.builder()
            .id(post.getId())
            .category(post.getCategory())
            .title(post.getTitle())
            .content(post.getContent())
            .authorNickname(post.getUser().getNickname())
            .authorProfileImage(s3Service.getPresignedUrl(post.getUser().getProfileImage()))
            .analysisImageUrl(post.getAnalysis() != null ? s3Service.getPresignedUrl(post.getAnalysis().getImageUrl()) : null)
            .analysisName(post.getAnalysis() != null ? post.getAnalysis().getName() : null)
            .postImageUrl(post.getImageUrl() != null ? s3Service.getPresignedUrl(post.getImageUrl()) : null)
            .likeCount(likeCount)
            .commentCount(comments.size())
            .isLiked(isLiked)
            .comments(comments)
            .createdAt(post.getCreatedAt())
            .build();
    }
}
