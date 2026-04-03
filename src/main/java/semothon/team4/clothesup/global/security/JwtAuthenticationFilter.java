package semothon.team4.clothesup.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final List<String> NO_AUTH_PATHS = List.of(
        "/auth/register",
        "/auth/login",
        "/swagger-ui/",
        "/v3/api-docs",
        "/swagger-resources",
        "/webjars/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // 인증이 필요 없는 경로는 바로 통과
        for (String noAuthPath : NO_AUTH_PATHS) {
            if (path.startsWith(noAuthPath)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String token = resolveToken(request);

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("인증 성공: {}", authentication.getName());
            }
        } catch (Exception e) {
            log.error("필터 내 인증 처리 중 오류 발생: {}", e.getMessage());
            // 필터에서 예외가 발생하면 서버가 터지지 않게 에러 응답을 직접 보냅니다.
            setErrorResponse(response, "인증에 실패했습니다: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\": \"401: " + message + "\", \"data\": null}");
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
