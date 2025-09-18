package online.coding_enthusiast.shop.coupons;

import online.coding_enthusiast.shop.coupons.data.Coupon;
import online.coding_enthusiast.shop.coupons.data.CouponUsage;
import online.coding_enthusiast.shop.coupons.data.CouponUsageId;
import online.coding_enthusiast.shop.coupons.helpers.AbstractMysqlTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class CouponUsagesRepositoryTest extends AbstractMysqlTest {

    @Autowired
    private CouponUsagesRepository sut;

    @Autowired
    private TestEntityManager entityManager;

    private Object persistCoupon(String code) {
        var c = new Coupon();
        c.setCode(code);
        c.setUsagesMax(10);
        c.setUsagesRemaining(5);
        c.setCountry("PL");
        c.setCreatedAt(LocalDate.now());
        entityManager.persistAndFlush(c);

        return c;
    }

    @Test
    @DisplayName("insertUsage - should allow different user-id / coupon-id combinations")
    void testInsertUsages_uniquely() {

        var coupon1 = (Coupon) persistCoupon("aa");
        var coupon2 = (Coupon) persistCoupon("bb");
        var coupon1Id = (int) entityManager.getId(coupon1);
        var coupon2Id = (int) entityManager.getId(coupon2);

//        sut.save(new CouponUsage(new CouponUsageId(coupon1Id, 1111), coupon2, LocalTime.now()));
//        sut.save(new CouponUsage(new CouponUsageId(coupon2Id, 2222), coupon2, LocalTime.now()));
//        sut.save(new CouponUsage(new CouponUsageId(coupon1Id, 2222), coupon1, LocalTime.now()));
//        sut.save(new CouponUsage(new CouponUsageId(coupon2Id, 1111), coupon2, LocalTime.now()));

        assertThat(sut.insertUsage(coupon1Id, 1111)).isEqualTo(1);
        assertThat(sut.insertUsage(coupon2Id, 2222)).isEqualTo(1);

        assertThat(sut.insertUsage(coupon1Id, 2222)).isEqualTo(1);
        assertThat(sut.insertUsage(coupon2Id, 1111)).isEqualTo(1);
    }

    @Test
    @DisplayName("insertUsage - should not allow duplicated keys")
    void testInsertUsages_uniquely2() {

        var coupon = persistCoupon("aa");
        var couponId = (int) entityManager.getId(coupon);

        Supplier<Integer> insert = () -> sut.insertUsage(couponId, 1111);

        assertThat(insert.get()).isEqualTo(1);

        assertThrows(DataIntegrityViolationException.class, insert::get);
    }
}
