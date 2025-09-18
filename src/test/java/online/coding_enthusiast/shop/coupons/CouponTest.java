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
public class CouponTest extends AbstractMysqlTest {

    @Autowired
    private CouponRepository sut;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("persist - should persist proper values")
    void testCouponPersist_completeObject() {

        final var code = "Code";
        final var usagesMax = 10;
        final var usagesRemaining = usagesMax - 2;
        final var country = "PL";
        final var createdAt = LocalDate.of(1998, 10, 28);

        var c = new Coupon();
        c.setCode(code);
        c.setUsagesMax(usagesMax);
        c.setUsagesRemaining(usagesRemaining);
        c.setCountry(country);
        c.setCreatedAt(createdAt);

        entityManager.persistAndFlush(c);

        var persisted = sut.findByCodeIgnoreCase(code);
        assertThat(persisted.isPresent()).isTrue();

        System.out.println("Persisted: " + persisted.get());
        assertThat(persisted.get().getCode().toUpperCase()).isEqualTo(code.toUpperCase());
        assertThat(persisted.get().getUsagesMax()).isEqualTo(usagesMax);
        assertThat(persisted.get().getUsagesRemaining()).isEqualTo(usagesRemaining);
        assertThat(persisted.get().getCountry()).isEqualTo(country);
        assertThat(persisted.get().getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("persist - should persist proper values of uninitialized creation-date and remaining-usages")
    void testCouponPersist_incompleteObject() {

        final var code = "Code";
        final var usagesMax = 10;
        final var country = "PL";

        var c = new Coupon();
        c.setCode(code);
        c.setUsagesMax(usagesMax);
        c.setCountry(country);

        entityManager.persistAndFlush(c);
        final var persistenceTime = LocalDate.now();

        var persisted = sut.findByCodeIgnoreCase(code);
        assertThat(persisted.isPresent()).isTrue();

        System.out.println("Persisted: " + persisted.get());
        assertThat(persisted.get().getCode().toUpperCase()).isEqualTo(code.toUpperCase());
        assertThat(persisted.get().getUsagesMax()).isEqualTo(usagesMax);
        assertThat(persisted.get().getUsagesRemaining()).isEqualTo(usagesMax);
        assertThat(persisted.get().getCountry()).isEqualTo(country);
        assertThat(persisted.get().getCreatedAt()).isEqualTo(persistenceTime);
    }

    @Test
    @DisplayName("persist - should calculate proper consumed-usages value")
    void testCouponPersist_consumed() {
        var c = new Coupon();
        c.setCode("Code");
        c.setCountry("PL");

        c.setUsagesMax(10);
        c.setUsagesRemaining(3);

        assertThat(c.getUsagesConsumed()).isEqualTo(7);
    }
}
