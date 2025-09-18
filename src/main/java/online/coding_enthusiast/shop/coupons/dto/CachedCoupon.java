package online.coding_enthusiast.shop.coupons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.coding_enthusiast.shop.coupons.data.Coupon;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedCoupon {
    private String id;
    private boolean exhausted;
    private String country;

    public CachedCoupon(Coupon c) {
        this.id = String.valueOf(c.getId());
        this.exhausted = c.isExhausted();
        this.country = c.getCountry();
    }

    public CachedCoupon(int id, boolean isExhausted, String country) {
        this.id = String.valueOf(id);
        this.exhausted = isExhausted;
        this.country = country;
    }
}