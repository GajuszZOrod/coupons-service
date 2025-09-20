package online.coding_enthusiast.shop.coupons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.coding_enthusiast.shop.coupons.data.Coupon;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedCoupon implements Serializable {
    private Integer id;
    private String code;
    private boolean exhausted;
    private String country;

    public CachedCoupon(Coupon c) {
        this.id = c.getId();
        this.code = c.getCode();
        this.exhausted = c.isExhausted();
        this.country = c.getCountry();
    }
}