package semothon.team4.clothesup.shop.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import semothon.team4.clothesup.shop.domain.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByPlaceId(String placeId);

    @Query(value = """
        SELECT * FROM shop
        WHERE (6371000 * acos(
            cos(radians(:lat)) * cos(radians(lat)) * cos(radians(lng) - radians(:lng))
            + sin(radians(:lat)) * sin(radians(lat))
        )) <= :radius
        """, nativeQuery = true)
    List<Shop> findShopsWithinRadius(@Param("lat") double lat, @Param("lng") double lng, @Param("radius") int radius);
}
