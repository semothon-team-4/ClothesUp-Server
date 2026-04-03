package semothon.team4.clothesup.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import semothon.team4.clothesup.global.common.BaseResponse;
import semothon.team4.clothesup.global.security.CustomUserDetails;
import semothon.team4.clothesup.user.domain.PostCategory;
import semothon.team4.clothesup.user.dto.postdto.CommentRequest;
import semothon.team4.clothesup.user.dto.postdto.PostCreateRequest;
import semothon.team4.clothesup.user.dto.postdto.PostListResponse;
import semothon.team4.clothesup.user.dto.postdto.PostResponse;
import semothon.team4.clothesup.user.service.PostService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "커뮤니티에 게시글을 작성합니다. 카테고리(LAUNDRY_TIP, REPAIR, RECOMMEND, CONDITION)를 포함해야 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "게시글 작성 성공")
    })
    @PostMapping
    public ResponseEntity<BaseResponse<Long>> createPost(
        @jakarta.validation.Valid @RequestBody PostCreateRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            log.error("인증 정보(UserDetails)가 null입니다.");
            return BaseResponse.unauthorized("인증 정보가 유효하지 않습니다.", null);
        }
        log.info("게시글 작성 요청 - 유저: {}, 제목: {}, 카테고리: {}", userDetails.getUser().getEmail(), request.getTitle(), request.getCategory());
        Long postId = postService.createPost(request, userDetails.getUser());
        return BaseResponse.created("게시글 작성 성공", postId);
    }

    @Operation(summary = "게시글 목록 조회", description = "커뮤니티의 게시글을 정렬 및 카테고리별로 조회합니다. category가 없으면 '전체' 조회입니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<PostListResponse>>> getPosts(
        @RequestParam(defaultValue = "LATEST") String sort,
        @RequestParam(required = false) PostCategory category,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PostListResponse> responses = postService.getPosts(userDetails != null ? userDetails.getUser() : null, sort, category);
        String categoryName = category == null ? "전체" : category.getDescription();
        return BaseResponse.ok("게시글 목록 조회 성공 (" + categoryName + " / " + sort + ")", responses);
    }

    @Operation(summary = "실시간 인기글 조회", description = "상단 슬라이드용 실시간 인기글을 카테고리별로 조회합니다. category가 없으면 '전체' 조회입니다.")
    @GetMapping("/popular")
    public ResponseEntity<BaseResponse<List<PostListResponse>>> getPopularPosts(
        @RequestParam(required = false) PostCategory category,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PostListResponse> responses = postService.getPopularPosts(userDetails != null ? userDetails.getUser() : null, category);
        return BaseResponse.ok("실시간 인기글 조회 성공", responses);
    }

    @Operation(summary = "게시글 검색", description = "키워드로 게시글을 검색합니다. 제목이나 내용에 키워드가 포함된 글을 최신순으로 반환합니다.")
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<PostListResponse>>> searchPosts(
        @RequestParam String keyword,
        @RequestParam(required = false) PostCategory category,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<PostListResponse> responses = postService.searchPosts(userDetails != null ? userDetails.getUser() : null, keyword, category);
        return BaseResponse.ok("게시글 검색 성공 (키워드: " + keyword + ")", responses);
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 내용과 댓글 리스트를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<BaseResponse<PostResponse>> getPostDetail(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PostResponse response = postService.getPostDetail(postId, userDetails != null ? userDetails.getUser() : null);
        return BaseResponse.ok("게시글 상세 조회 성공", response);
    }

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @PostMapping("/{postId}/comments")
    public ResponseEntity<BaseResponse<Void>> addComment(
        @PathVariable Long postId,
        @RequestBody CommentRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            return BaseResponse.unauthorized("인증 정보가 유효하지 않습니다.", null);
        }
        postService.addComment(postId, request, userDetails.getUser());
        return BaseResponse.ok("댓글 작성 성공", null);
    }

    @Operation(summary = "좋아요 토글", description = "게시글의 좋아요를 등록하거나 취소합니다.")
    @PostMapping("/{postId}/like")
    public ResponseEntity<BaseResponse<Boolean>> toggleLike(
        @PathVariable Long postId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            return BaseResponse.unauthorized("인증 정보가 유효하지 않습니다.", null);
        }
        boolean isLiked = postService.toggleLike(postId, userDetails.getUser());
        String message = isLiked ? "좋아요 등록 성공" : "좋아요 취소 성공";
        return BaseResponse.ok(message, isLiked);
    }
}
