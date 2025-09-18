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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

@Testcontainers
public class CouponsControllerPolandIntegrationTest extends AbstractMysqlRedisWiremockTest {

    private final static String COUNTRY_SUPPORTED_1 = "PL";
    private final static String COUNTRY_SUPPORTED_2 = "DE";
    private final static String COUNTRY_UNSUPPORTED = "RU";
    private final static String SHARD_COUNTRIES = COUNTRY_SUPPORTED_1 + "," + COUNTRY_SUPPORTED_2;

    private final static String IP_SUPPORTED_1 = "99.0.0.1"; // PL
    private final static String IP_SUPPORTED_2 = "99.0.0.2"; // DE
    private final static String IP_UNSUPPORTED = "99.0.0.3"; // RU

    private final static Map<String, String> IP_BY_COUNTRIES;
    static
    {
        IP_BY_COUNTRIES = new HashMap<>();
        IP_BY_COUNTRIES.put(COUNTRY_SUPPORTED_1, IP_SUPPORTED_1);
        IP_BY_COUNTRIES.put(COUNTRY_SUPPORTED_2, IP_SUPPORTED_2);
        IP_BY_COUNTRIES.put(COUNTRY_UNSUPPORTED, IP_UNSUPPORTED);
    };

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
    @DisplayName("coupons - should not allow creating coupon of unsupported country")
    void createCoupon_unsupportedCountry() {

        var request = new CreateCouponRequest("Wiosna", 10, COUNTRY_UNSUPPORTED);

        new CreateCouponTester()
                .send(request)
                .expect(HttpStatus.CONFLICT)
                .expect("Provided country is not supported by this instance of the service.");

        new CreateCouponTester()
                .send(request)
                .expect(HttpStatus.CONFLICT)
                .expect("Provided country is not supported by this instance of the service.");
    }

    @Test
    @DisplayName("coupons - should allow to create coupon of supported country")
    void createCoupon_supportedCountry() {

        var request = new CreateCouponRequest("Wiosna", 10, COUNTRY_SUPPORTED_1);

        new CreateCouponTester()
                .send(request)
                .expectStatusOk()
                .expect("Coupon created successfully.");
    }

    @Test
    @DisplayName("coupons - should not allow creating coupons with the same code")
    void createCoupon_duplicated() {

        final var code = "Wiosna";

        var request1 = new CreateCouponRequest(code, 10, COUNTRY_SUPPORTED_1);
        var request2 = new CreateCouponRequest(code, 15, COUNTRY_SUPPORTED_1);

        new CreateCouponTester()
                .send(request1)
                .expectStatusOk()
                .expect("Coupon created successfully.");

        new CreateCouponTester()
                .send(request2)
                .expectStatusConflict()
                .expect("Coupon with given code already exists.");
    }

    @Test
    @DisplayName("coupons - should not allow creating duplicated coupons, if the only difference in their codes is letters case")
    void createCoupon_duplicatedCaseInsensitive() {

        final var code = "Wiosna";
        final var codeToUpper = code.toUpperCase();
        final var codeToLower = code.toUpperCase();

        var request1 = new CreateCouponRequest(codeToUpper, 10, COUNTRY_SUPPORTED_1);
        var request2 = new CreateCouponRequest(codeToLower, 500, COUNTRY_SUPPORTED_2);

        new CreateCouponTester()
                .send(request1)
                .expectStatusOk()
                .expect("Coupon created successfully.");

        new CreateCouponTester()
                .send(request2)
                .expectStatusConflict()
                .expect("Coupon with given code already exists.");
    }

    @Test
    @DisplayName("coupons-usages - should allow to use created coupon from within its country")
    void useCoupon_withinCountry() {

        final var code = "Wiosna";

        final var sameCountry = COUNTRY_SUPPORTED_1;

        var requestCreate = new CreateCouponRequest(code, 10, sameCountry);
        var requestUse = new UseCouponRequest(code, 0, IP_BY_COUNTRIES.get(sameCountry));

        new CreateCouponTester()
                .send(requestCreate)
                .expectStatusOk()
                .expect("Coupon created successfully.");

        new UseCouponTester()
                .send(requestUse)
                .expectStatusOk()
                .expect("Coupon used successfully.");
    }

    @Test
    @DisplayName("coupons-usages - should not allow to use coupon from outside its country")
    void useCoupon_outsideOfItsCountry() {

        final var code = "Wiosna";

        var requestCreate = new CreateCouponRequest(code, 10, COUNTRY_SUPPORTED_1);
        var requestUse = new UseCouponRequest(code, 0, IP_SUPPORTED_2);

        new CreateCouponTester()
                .send(requestCreate)
                .expectStatusOk()
                .expect("Coupon created successfully.");

        new UseCouponTester()
                .send(requestUse)
                .expectStatusConflict()
                .expect("The coupon cannot be used from user's country.");
    }

    @Test
    @DisplayName("coupons-usages - should not allow to use coupon of unsupported country")
    void useCoupon_ofUnsupportedCountry() {

        final var code = "Wiosna";

        var requestCreate = new CreateCouponRequest(code, 10, COUNTRY_SUPPORTED_1);
        var requestUse = new UseCouponRequest(code, 0, IP_UNSUPPORTED);

        new CreateCouponTester()
                .send(requestCreate)
                .expectStatusOk()
                .expect("Coupon created successfully.");

        new UseCouponTester()
                .send(requestUse)
                .expectStatusConflict()
                .expect("Provided country is not supported by this instance of the service.");
    }

    @Test
    @DisplayName("coupons-usages - should not allow to use nonexistent coupon")
    void useCoupon_nonexistent() {

        var requestUse = new UseCouponRequest("NonExisting", 111, IP_SUPPORTED_1);
        new UseCouponTester()
                .send(requestUse)
                .expect("Coupon with given code not found.")
                .expect(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("coupons-usages - should not allow to use coupon upon its exhaustion")
    void useCoupon_exhausted() {

        final var code = "Wiosna";
        final var country = COUNTRY_SUPPORTED_1;

        var requestCreate = new CreateCouponRequest(code, 2, country);
        new CreateCouponTester()
                .send(requestCreate)
                .expectStatusOk()
                .expect("Coupon created successfully.");

        var requestUse1 = new UseCouponRequest(code, 111, IP_BY_COUNTRIES.get(country));
        new UseCouponTester()
                .send(requestUse1)
                .expectStatusOk()
                .expect("Coupon used successfully.");

        var requestUse2 = new UseCouponRequest(code, 222, IP_BY_COUNTRIES.get(country));
        new UseCouponTester()
                .send(requestUse2)
                .expectStatusOk()
                .expect("Coupon used successfully.");

        var requestUse3 = new UseCouponRequest(code, 333, IP_BY_COUNTRIES.get(country));
        new UseCouponTester()
                .send(requestUse3)
                .expectStatusConflict()
                .expect("The coupon has no remaining usages.");

        var requestUse4 = new UseCouponRequest(code, 444, IP_BY_COUNTRIES.get(country));
        new UseCouponTester()
                .send(requestUse4)
                .expectStatusConflict()
                .expect("The coupon has no remaining usages.");
    }

    @Test
    @DisplayName("coupons-usages - should not allow one user to use coupon multiple times")
    void useCoupon_sameUser() {

        final var code = "Wiosna";
        final var country = COUNTRY_SUPPORTED_1;

        var requestCreate = new CreateCouponRequest(code, 10, country);
        new CreateCouponTester()
                .send(requestCreate)
                .expectStatusOk()
                .expect("Coupon created successfully.");

        var requestUse = new UseCouponRequest(code, 111, IP_BY_COUNTRIES.get(country));

        new UseCouponTester()
                .send(requestUse)
                .expectStatusOk()
                .expect("Coupon used successfully.");

        new UseCouponTester()
                .send(requestUse)
                .expectStatusConflict()
                .expect("The user has already used the coupon.");
    }




}

// TODO: implement a builder for requests, to avoid mistakes