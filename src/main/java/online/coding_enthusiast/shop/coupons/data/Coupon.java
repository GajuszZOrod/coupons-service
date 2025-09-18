package online.coding_enthusiast.shop.coupons.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Coupon {

    // TODO: consider using UUID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "usages_max", nullable = false)
    private Integer usagesMax;

    @Column(name = "usages_remaining", nullable = false)
    private Integer usagesRemaining;

    @Column(name = "country", nullable = false)
    private String country;

    @PrePersist
    public void init() {
        if (usagesRemaining == null) {
            usagesRemaining = usagesMax;
        }
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }

    public int getUsagesConsumed() {
        if (usagesMax == null || usagesRemaining == null) {
            throw new IllegalStateException();
        }
        return usagesMax - usagesRemaining;
    }

    public boolean isExhausted() {
        if (usagesMax == null || usagesRemaining == null) {
            throw new IllegalStateException();
        }
        return usagesRemaining <= 0;
    }
}
