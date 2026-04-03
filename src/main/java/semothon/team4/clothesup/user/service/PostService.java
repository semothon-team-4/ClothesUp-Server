package semothon.team4.clothesup.user.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import semothon.team4.clothesup.analysis.domain.Analysis;
import semothon.team4.clothesup.analysis.repository.AnalysisRepository;
import semothon.team4.clothesup.global.exception.CoreException;
import semothon.team4.clothesup.global.exception.code.CommonErrorCode;
import semothon.team4.clothesup.user.domain.Comment;
import semothon.team4.clothesup.user.domain.Post;
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

    @Transactional
    public Long createPost(PostCreateRequest request, User user) {
        if (user == null || user.getId() == null) {
            throw new CoreException(semothon.team4.clothesup.global.exception.code.UserErrorCode.USER_NOT_FOUND);
        }
        User persistentUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new CoreException(semothon.team4.clothesup.global.exception.code.UserErrorCode.USER_NOT_FOUND));

        Analysis analysis = null;
        if (request.getAnalysisId() != null) {
            analysis = analysisRepository.findById(request.getAnalysisId())
                .orElseThrow(() -> new CoreException(CommonErrorCode.RESOURCE_NOT_FOUND));
        }

        Post post = new Post(persistentUser, analysis, request.getTitle(), request.getContent(), request.isPublic());
        return postRepository.save(post).getId();
    }

    // [목록 조회] 댓글 목록을 제외하고 개수만 포함
    public List<PostListResponse> getPosts(User user, String sort) {
        User persistentUser = user != null ? userRepository.findById(user.getId()).orElse(null) : null;
        
        List<Post> posts;
        if ("POPULAR".equalsIgnoreCase(sort)) {
            posts = postRepository.findAllOrderByLikesDesc();
        } else {
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        }

        return posts.stream()
            .map(post -> convertToPostListResponse(post, persistentUser))
            .collect(Collectors.toList());
    }

    // [실시간 인기글] 상단 슬라이드용 (댓글 목록 제외)
    public List<PostListResponse> getPopularPosts(User user) {
        User persistentUser = user != null ? userRepository.findById(user.getId()).orElse(null) : null;
        return postRepository.findAllOrderByLikesDesc().stream()
            .limit(5)
            .map(post -> convertToPostListResponse(post, persistentUser))
            .collect(Collectors.toList());
    }

    // [상세 조회] 모든 정보와 댓글 목록 포함 (본문 전체 노출)
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
            .title(post.getTitle())
            .content(summaryContent) // 요약된 내용 전달
            .authorNickname(post.getUser().getNickname())
            .authorProfileImage(post.getUser().getProfileImage())
            .analysisImageUrl(post.getAnalysis() != null ? post.getAnalysis().getImageUrl() : null)
            .analysisName(post.getAnalysis() != null ? post.getAnalysis().getName() : null)
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
                .authorProfileImage(comment.getUser().getProfileImage())
                .createdAt(comment.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        boolean isLiked = user != null && postLikeRepository.existsByPostAndUser(post, user);
        long likeCount = postLikeRepository.countByPost(post);

        return PostResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent()) // 전체 내용 전달
            .authorNickname(post.getUser().getNickname())
            .authorProfileImage(post.getUser().getProfileImage())
            .analysisImageUrl(post.getAnalysis() != null ? post.getAnalysis().getImageUrl() : null)
            .analysisName(post.getAnalysis() != null ? post.getAnalysis().getName() : null)
            .likeCount(likeCount)
            .commentCount(comments.size())
            .isLiked(isLiked)
            .comments(comments)
            .createdAt(post.getCreatedAt())
            .build();
    }
}
