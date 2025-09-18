package online.coding_enthusiast.shop.coupons.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_usages")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CouponUsage {

    @EmbeddedId
    private CouponUsageId id;

    @ManyToOne()
    @MapsId("couponId")
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @PrePersist
    public void init() {
        usedAt = LocalDateTime.now();
    }

}
