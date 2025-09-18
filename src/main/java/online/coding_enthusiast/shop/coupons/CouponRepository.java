package online.coding_enthusiast.shop.coupons;

import online.coding_enthusiast.shop.coupons.data.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Coupon c SET c.usagesRemaining = c.usagesRemaining - 1 WHERE c.code = :code AND c.usagesRemaining > 0")
    int decrementRemaining(@Param("code") String code);

    Optional<Coupon> findByCodeIgnoreCase(String code);
//    boolean existsByCodeIgnoreCase(String code);
}
