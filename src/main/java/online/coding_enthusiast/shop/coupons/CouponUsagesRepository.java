package online.coding_enthusiast.shop.coupons;

import online.coding_enthusiast.shop.coupons.data.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponUsagesRepository extends JpaRepository<CouponUsage, Integer> {

    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO coupon_usages (coupon_id, user_id, used_at) VALUES (:couponId, :userId, NOW())", nativeQuery = true)
    int insertUsage(@Param("couponId") int couponId, @Param("userId") int userId);

}
