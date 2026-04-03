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
import semothon.team4.clothesup.user.dto.postdto.CommentRequest;
import semothon.team4.clothesup.user.dto.postdto.CommentResponse;
import semothon.team4.clothesup.user.dto.postdto.PostCreateRequest;
import semothon.team4.clothesup.user.dto.postdto.PostResponse;
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

    // 정렬 기반 피드 조회 (최신순, 인기순)
    public List<PostResponse> getPosts(User user, String sort) {
        User persistentUser = user != null ? userRepository.findById(user.getId()).orElse(null) : null;
        
        List<Post> posts;
        if ("POPULAR".equalsIgnoreCase(sort)) {
            posts = postRepository.findAllOrderByLikesDesc();
        } else {
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        }

        return posts.stream()
            .map(post -> convertToPostResponse(post, persistentUser))
            .collect(Collectors.toList());
    }

    // 실시간 인기글 (상단 슬라이드용 - 상위 5개만)
    public List<PostResponse> getPopularPosts(User user) {
        User persistentUser = user != null ? userRepository.findById(user.getId()).orElse(null) : null;
        return postRepository.findAllOrderByLikesDesc().stream()
            .limit(5)
            .map(post -> convertToPostResponse(post, persistentUser))
            .collect(Collectors.toList());
    }

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
            .content(post.getContent())
            .authorNickname(post.getUser().getNickname())
            .authorProfileImage(post.getUser().getProfileImage())
            .analysisImageUrl(post.getAnalysis() != null ? post.getAnalysis().getImageUrl() : null)
            .analysisName(post.getAnalysis() != null ? post.getAnalysis().getName() : null)
            .likeCount(likeCount)
            .commentCount(comments.size()) // 댓글 수 추가
            .isLiked(isLiked)
            .comments(comments)
            .createdAt(post.getCreatedAt())
            .build();
    }
}
