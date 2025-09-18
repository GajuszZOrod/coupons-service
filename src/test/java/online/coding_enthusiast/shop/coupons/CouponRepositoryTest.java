package online.coding_enthusiast.shop.coupons;

import online.coding_enthusiast.shop.coupons.data.Coupon;
import online.coding_enthusiast.shop.coupons.helpers.AbstractMysqlTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CouponRepositoryTest extends AbstractMysqlTest {


    @Autowired
    private CouponRepository sut;

    @Autowired
    private TestEntityManager entityManager;

    private void persistCoupon(String code, int usagesRemaining, int usagesMax, String country, LocalDate createdAt) {
        var c = new Coupon();
        c.setCode(code);
        c.setUsagesMax(usagesMax);
        c.setUsagesRemaining(usagesRemaining);
        c.setCountry(country);
        c.setCreatedAt(createdAt);
        entityManager.persistAndFlush(c);
    }

    private void persistCoupon(String code, int usagesRemaining, int usagesMax, String country) {
        persistCoupon(code, usagesRemaining, usagesMax, country, null);
    }

    @Test
    @DisplayName("decrementRemaining - should treat coupon code in case-insensitive manner")
    void testDecrementRemaining_decreased() {

        final var codeMixed = "lower_case_CODE";
        final var codeUpper = codeMixed.toUpperCase();

        persistCoupon(codeMixed, 10, 10, "PL");

        assertThat(sut.decrementRemaining(codeUpper)).isEqualTo(1);
    }

    @Test
    @DisplayName("decrementRemaining - should decrement remaining usages value")
    void testDecrementRemaining_decreased2() {

        final var code = "code";
        final var createdAt = LocalDate.now();
        final var country = "PL";
        final var usagesMaxOriginal = 10;
        final var usagesRemainingOriginal = 8;

        final var usagesToPerform = 3;
        final var usagesRemainingExpectedAfterUse = usagesRemainingOriginal - usagesToPerform;

        persistCoupon(code, usagesRemainingOriginal, usagesMaxOriginal, country, createdAt);

        for (int i = 0; i < usagesToPerform; i++ ) {
            assertThat(sut.decrementRemaining(code)).isEqualTo(1);
            System.out.println("decrementing iteration " + i);
        }

        var modifiedCoupon = sut.findByCodeIgnoreCase(code);
        assertThat(modifiedCoupon.isPresent()).isTrue();

        System.out.println("Modified: " + modifiedCoupon);

        assertThat(modifiedCoupon.get().getUsagesRemaining()).isEqualTo(usagesRemainingExpectedAfterUse);

        assertThat(modifiedCoupon.get().getUsagesMax()).isEqualTo(usagesMaxOriginal);
        assertThat(modifiedCoupon.get().getCode()).isEqualTo(code);
        assertThat(modifiedCoupon.get().getCreatedAt()).isEqualTo(createdAt);
        assertThat(modifiedCoupon.get().getCountry()).isEqualTo(country);
    }

    @Test
    @DisplayName("decrementRemaining - should not decrement below zero")
    void testDecrementRemaining_decreased4() {

        final var code = "code";
        persistCoupon(code, 2, 100, "PL");

        var createdCoupon = sut.findByCodeIgnoreCase(code);
        assertThat(createdCoupon.isPresent()).isTrue();

        assertThat(sut.decrementRemaining(code)).isEqualTo(1);
        assertThat(sut.decrementRemaining(code)).isEqualTo(1);
        assertThat(sut.decrementRemaining(code)).isEqualTo(0);
        assertThat(sut.decrementRemaining(code)).isEqualTo(0);

        var modifiedCoupon = sut.findByCodeIgnoreCase(code);
        assertThat(modifiedCoupon.isPresent()).isTrue();

        assertThat(modifiedCoupon.get().getUsagesRemaining()).isEqualTo(0);
    }
}
