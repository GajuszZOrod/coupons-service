package online.coding_enthusiast.shop.coupons.dto;

import lombok.Data;

@Data
public class CouponInfo {
    private int id;
    private String code;
    private String country;
    private boolean exhausted;

    public CouponInfo(CachedCoupon cachedCoupon, String code) {
        this.id = Integer.parseInt(cachedCoupon.getId());
        this.country = cachedCoupon.getCountry();
        this.code = code;
        this.exhausted = cachedCoupon.isExhausted();
    }
}
