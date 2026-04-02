package semothon.team4.clothesup.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placeId;

    @Column(nullable = false)
    private String name;

    private String address;
    private Double lat;
    private Double lng;

    private LocalTime openTime;
    private LocalTime closeTime;

    private Double rate;
    private Long like;
    private Long reviewCount;

    //세탁소, 수선집
    private String category;

    private String description;
    private String phone;
    private String imageUrl;
    private LocalDateTime createdAt;
}
