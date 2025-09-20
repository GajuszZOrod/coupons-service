package online.coding_enthusiast.shop.coupons;

import lombok.RequiredArgsConstructor;
import online.coding_enthusiast.shop.coupons.dto.CachedCoupon;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponsCache {

    private final CouponRepository couponRepository;

    @Cacheable(value = "couponByCode", unless = "#result == null")
    public Optional<CachedCoupon> getCachedCoupon(String code) {
        return couponRepository.findByCodeIgnoreCase(code)
                .map(CachedCoupon::new);
    }

    @CachePut(value = "couponByCode", key = "#coupon.getCode()")
    public CachedCoupon cacheCoupon(CachedCoupon coupon) {
        return coupon;
    }
}
