package online.coding_enthusiast.shop.coupons;

import jakarta.transaction.Transactional;
import online.coding_enthusiast.shop.coupons.dto.CreateCouponRequest;
import online.coding_enthusiast.shop.coupons.dto.UseCouponRequest;
import online.coding_enthusiast.shop.coupons.helpers.AbstractMysqlRedisWiremockTest;
import online.coding_enthusiast.shop.coupons.helpers.CreateCouponTester;
import online.coding_enthusiast.shop.coupons.helpers.UseCouponTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


public class CouponsControllerAllCountriesIntegrationTest extends AbstractMysqlRedisWiremockTest {

    private final static String SHARD_COUNTRIES = "";

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("coupons.supportedCountries", () -> SHARD_COUNTRIES);
    }

    @BeforeEach
    @Transactional
    void prepareTest() {
        clearCache();
        clearDatabase();
    }

    @Test
    @DisplayName("coupons - can create coupon of any country in application with empty supportedCountries list")
    void createCoupon_supportedCountry() {

        final var country1 = "PL";
        final var country2 = "CZ";
        final var request1 = new CreateCouponRequest("Wiosna", 10, country1);
        final var request2 = new CreateCouponRequest("Jesien", 10, country2);

        final var expectedCode = HttpStatus.OK;
        final var expectedMsg = "Coupon created successfully.";

        new CreateCouponTester()
                .send(request1)
                .expect(expectedCode)
                .expect(expectedMsg);

        new CreateCouponTester()
                .send(request2)
                .expect(expectedCode)
                .expect(expectedMsg);
    }

    @Test
    @DisplayName("coupons - malformed request")
    void createCoupon_malformed() {

        var request1 = new CreateCouponRequest("code", 10, null);

        new CreateCouponTester()
                .send(request1)
                .expect("Request malformed.")
                .expect(HttpStatus.BAD_REQUEST);
    }

}
