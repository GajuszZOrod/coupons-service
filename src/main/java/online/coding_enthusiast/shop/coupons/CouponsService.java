package online.coding_enthusiast.shop.coupons;

import jakarta.transaction.Transactional;
import online.coding_enthusiast.shop.coupons.data.Coupon;
import online.coding_enthusiast.shop.coupons.dto.CachedCoupon;
import online.coding_enthusiast.shop.coupons.dto.CouponInfo;
import online.coding_enthusiast.shop.coupons.dto.CreateCouponRequest;
import online.coding_enthusiast.shop.coupons.dto.UseCouponRequest;
import online.coding_enthusiast.shop.coupons.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Service
public class CouponsService {

    // TODO: clear old entries from coupon_usage database?

    private final AppProperties appProperties;
    private final CouponRepository couponRepository;
    private final CouponUsagesRepository couponUsagesRepository;
    private final GeoLocalizationService geoLocalizationService;

    TransactionTemplate transactionTemplate;
    public CouponsService(PlatformTransactionManager ptm,
                          AppProperties appProperties,
                          CouponRepository couponRepository,
                          CouponUsagesRepository couponUsagesRepository,
                          GeoLocalizationService geoLocalizationService) {
        this.transactionTemplate = new TransactionTemplate(ptm);
        this.appProperties = appProperties;
        this.couponRepository = couponRepository;
        this.couponUsagesRepository = couponUsagesRepository;
        this.geoLocalizationService = geoLocalizationService;
    }

    public String createCoupon(CreateCouponRequest req) {

        var normalizedCode = req.code().toUpperCase();

        if (!isCountrySupported(req.country())) {
            throw new CountryUnsupportedException();
        }

        var existingCoupon = getCouponInfo(normalizedCode);
        if (existingCoupon.isPresent()) {
            throw new CouponAlreadyExistsException();
        }

        var createdCoupon = new Coupon();
        createdCoupon.setCode(normalizedCode);
        createdCoupon.setUsagesRemaining(req.usagesMax());
        createdCoupon.setUsagesMax(req.usagesMax());
        createdCoupon.setCountry(req.country());
        couponRepository.save(createdCoupon);

        cacheCoupon(createdCoupon);

        return "Coupon created successfully.";
    }

    public String useCoupon(UseCouponRequest req) {

        var normalizedCode = req.code().toUpperCase();

        var userCountry = geoLocalizationService.getCountryCode(req.userIpAddress());
        if (!isCountrySupported(userCountry)) {
            throw new CountryUnsupportedException();
        }

        var couponInfo = getCouponInfo(normalizedCode);

        if (couponInfo.isEmpty()) {
            throw new CouponNotFoundException();
        }

        if (couponInfo.get().isExhausted()) {
            throw new CouponExhaustedException();
        }

        if (!couponInfo.get().getCountry().equals(userCountry)) {
            throw new CouponUnavailableForUserCountryException();
        }

        useCoupon(couponInfo.get(), req.userId());

        return "Coupon used successfully.";
    }

    private Optional<CouponInfo> getCouponInfo(String code) {
        var coupon = getCachedCoupon(code);
        return coupon.map(c -> new CouponInfo(c, code));
    }

    @Cacheable(value = "couponByCode", unless = "#result.isEmpty()")
    private Optional<CachedCoupon> getCachedCoupon(String code) {
        return couponRepository.findByCodeIgnoreCase(code)
                .map(CachedCoupon::new);
    }

    @CachePut(value = "couponByCode", key = "#toCache.getCode()")
    private CachedCoupon cacheCoupon(Coupon toCache) {
        return new CachedCoupon(toCache);
    }

    @CachePut(value = "couponByCode", key = "#code")
    private CachedCoupon cacheCoupon(String code, CachedCoupon toCache) {
        return toCache;
    }

    private void useCoupon(CouponInfo couponInfo, int userId) {
        transactionTemplate.executeWithoutResult(status -> {
            try {
                couponUsagesRepository.insertUsage(couponInfo.getId(), userId);

                var updated = couponRepository.decrementRemaining(couponInfo.getCode());
                if (updated == 0) {
                    var exhaustedCoupon = new CachedCoupon(couponInfo.getId(), true, couponInfo.getCountry());
                    cacheCoupon(couponInfo.getCode(), exhaustedCoupon);

                    throw new CouponExhaustedException();
                }
            }
            catch (DataIntegrityViolationException e) {
                throw new CouponAlreadyUsedException(); // TODO: check whether the error is about key uniqueness, or something else
            }
        });
    }

    /**
     * Checks whether this application instance supports coupons related to a particular country.
     * Application can be configured to support a limited number of countries.
     */
    private boolean isCountrySupported(String country) {

        var supportedCountries = appProperties.getSupportedCountries();
        return supportedCountries.isEmpty() || supportedCountries.contains(country);
    }
}
