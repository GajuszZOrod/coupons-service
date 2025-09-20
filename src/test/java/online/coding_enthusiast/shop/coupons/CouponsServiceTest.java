package online.coding_enthusiast.shop.coupons;

import online.coding_enthusiast.shop.coupons.data.Coupon;
import online.coding_enthusiast.shop.coupons.dto.CachedCoupon;
import online.coding_enthusiast.shop.coupons.dto.CreateCouponRequest;
import online.coding_enthusiast.shop.coupons.dto.UseCouponRequest;
import online.coding_enthusiast.shop.coupons.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponsServiceTest {

    private CouponRepository couponRepository;
    private CouponUsagesRepository couponUsagesRepository;
    private GeoLocalizationService geoLocalizationService;
    private AppProperties appProperties;
    private CouponsService couponsService;
    private CouponsCache couponsCache;

    private static final String COUNTRY_SUPPORTED_1 = "PL";
    private static final String IP_SUPPORTED_1 = "99.0.0.1";
    private static final String COUNTRY_SUPPORTED_2 = "DE";
    private static final String IP_SUPPORTED_2 = "99.0.0.2";
    private static final String COUNTRY_UNSUPPORTED = "RU";
    private static final String IP_UNSUPPORTED = "99.0.0.3";

    @BeforeEach
    void setup() {
        PlatformTransactionManager ptm = mock(PlatformTransactionManager.class);
        appProperties = mock(AppProperties.class);
        couponRepository = mock(CouponRepository.class);
        couponUsagesRepository = mock(CouponUsagesRepository.class);
        geoLocalizationService = mock(GeoLocalizationService.class);
        couponsCache = mock(CouponsCache.class);
        couponsService = new CouponsService(ptm, appProperties, couponRepository, couponUsagesRepository, geoLocalizationService, couponsCache);

        when(appProperties.getSupportedCountries()).thenReturn(List.of(COUNTRY_SUPPORTED_1, COUNTRY_SUPPORTED_2));

        when(geoLocalizationService.getCountryCode(IP_SUPPORTED_1)).thenReturn(COUNTRY_SUPPORTED_1);
        when(geoLocalizationService.getCountryCode(IP_SUPPORTED_2)).thenReturn(COUNTRY_SUPPORTED_2);
        when(geoLocalizationService.getCountryCode(IP_UNSUPPORTED)).thenReturn(COUNTRY_UNSUPPORTED);
    }

    @Test
    void createCoupon_unsupportedCountry_throws() {
        var req = new CreateCouponRequest("Wiosna", 10, COUNTRY_UNSUPPORTED);

        assertThatThrownBy(() -> couponsService.createCoupon(req))
                .isInstanceOf(CountryUnsupportedException.class);
    }

    @Test
    void createCoupon_alreadyExists_throws() {
        final var code = "Wiosna";
        final var normalizedCode = "WIOSNA";

        var req = new CreateCouponRequest(code, 10, COUNTRY_SUPPORTED_1);
        when(couponsCache.getCachedCoupon(normalizedCode))
                .thenReturn(Optional.of(
                        new CachedCoupon(1,
                                normalizedCode,
                                false,
                                COUNTRY_SUPPORTED_1)
                ));
        assertThatThrownBy(() -> couponsService.createCoupon(req))
                .isInstanceOf(CouponAlreadyExistsException.class);
    }

    @Test
    void createCoupon_success() {
        var req = new CreateCouponRequest("Wiosna", 10, COUNTRY_SUPPORTED_1);
        when(couponsCache.getCachedCoupon("WIOSNA")).thenReturn(Optional.empty());

        var result = couponsService.createCoupon(req);

        assertThat(result).isEqualTo("Coupon created successfully.");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void useCoupon_countryUnsupported_throws() {
        var req = new UseCouponRequest("Wiosna", 1111, IP_UNSUPPORTED);

        assertThatThrownBy(() -> couponsService.useCoupon(req))
                .isInstanceOf(CountryUnsupportedException.class);
    }

    @Test
    void useCoupon_notFound_throws() {
        var req = new UseCouponRequest("Wiosna", 1111, IP_SUPPORTED_1);
        when(couponsCache.getCachedCoupon("WIOSNA")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponsService.useCoupon(req))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    void useCoupon_exhausted_throws() {
        final var code = "Wiosna";
        final var normalizedCode = "WIOSNA";

        var req = new UseCouponRequest(code, 1111, IP_SUPPORTED_1);

        var coupon = new CachedCoupon();
        coupon.setId(1);
        coupon.setExhausted(true);
        coupon.setCode(normalizedCode);
        coupon.setCountry(COUNTRY_SUPPORTED_1);
        when(couponsCache.getCachedCoupon(normalizedCode)).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponsService.useCoupon(req))
                .isInstanceOf(CouponExhaustedException.class);
    }

    @Test
    void useCoupon_wrongCountry_throws() {
        final var code = "Wiosna";
        final var normalizedCode = "WIOSNA";

        var req = new UseCouponRequest(code, 1111, IP_SUPPORTED_1);

        var coupon = new CachedCoupon();
        coupon.setId(1);
        coupon.setExhausted(false);
        coupon.setCode(normalizedCode);
        coupon.setCountry(COUNTRY_SUPPORTED_2);
        when(couponsCache.getCachedCoupon(normalizedCode)).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponsService.useCoupon(req))
                .isInstanceOf(CouponUnavailableForUserCountryException.class);
    }

    @Test
    void useCoupon_success() {
        final var userId = 1111;
        final var couponId = 1;

        final var code = "Wiosna";
        final var normalizedCode = "WIOSNA";

        var req = new UseCouponRequest(code, userId, IP_SUPPORTED_1);

        when(couponsCache.getCachedCoupon(normalizedCode))
                .thenReturn(Optional.of(
                        new CachedCoupon(couponId,
                                normalizedCode,
                                false,
                                COUNTRY_SUPPORTED_1)
                ));
        when(couponUsagesRepository.insertUsage(anyInt(), anyInt())).thenReturn(1);
        when(couponRepository.decrementRemaining(normalizedCode)).thenReturn(1);

        var result = couponsService.useCoupon(req);

        assertThat(result).isEqualTo("Coupon used successfully.");
        verify(couponUsagesRepository).insertUsage(anyInt(), eq(userId));
        verify(couponRepository).decrementRemaining(normalizedCode);
    }

    @Test
    void useCoupon_alreadyUsed_throws() {
        final var userId = 1111;
        final var couponId = 1;

        final var code = "Wiosna";
        final var normalizedCode = "WIOSNA";

        var req = new UseCouponRequest(code, userId, IP_SUPPORTED_1);

        var coupon = new CachedCoupon();
        coupon.setId(couponId);
        coupon.setExhausted(false);
        coupon.setCode(normalizedCode);
        coupon.setCountry(COUNTRY_SUPPORTED_1);
        when(couponsCache.getCachedCoupon(normalizedCode)).thenReturn(Optional.of(coupon));
        doThrow(DataIntegrityViolationException.class).when(couponUsagesRepository).insertUsage(couponId, userId);

        assertThatThrownBy(() -> couponsService.useCoupon(req))
                .isInstanceOf(CouponAlreadyUsedException.class);
    }
}
