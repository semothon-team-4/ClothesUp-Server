package semothon.team4.clothesup.global.kakao;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import semothon.team4.clothesup.shop.dto.KakaoSearchResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoLocalApiClient {

    @Value("${kakao.api.key}")
    private String apiKey;

    @Value("${kakao.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public KakaoSearchResponse searchKeyword(String query, int size, double lat, double lng) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);

        URI uri = UriComponentsBuilder
            .fromUriString(apiUrl)
            .queryParam("query", "{query}")
            .queryParam("y", lat)
            .queryParam("x", lng)
            .queryParam("size", size)
            .encode()
            .buildAndExpand(query)
            .toUri();

        log.info("요청 URI: {}", uri);

        return restTemplate.exchange(
            uri,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            KakaoSearchResponse.class
        ).getBody();
    }
}