package online.coding_enthusiast.shop.coupons;

import online.coding_enthusiast.shop.coupons.data.Coupon;
import online.coding_enthusiast.shop.coupons.dto.CachedCoupon;
import online.coding_enthusiast.shop.coupons.dto.CreateCouponRequest;
import online.coding_enthusiast.shop.coupons.dto.UseCouponRequest;
import online.coding_enthusiast.shop.coupons.exceptions.*;
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
    private final CouponsCache couponsCache;

    TransactionTemplate transactionTemplate;
    public CouponsService(PlatformTransactionManager ptm,
                          AppProperties appProperties,
                          CouponRepository couponRepository,
                          CouponUsagesRepository couponUsagesRepository,
                          GeoLocalizationService geoLocalizationService,
                          CouponsCache couponsCache) {
        this.transactionTemplate = new TransactionTemplate(ptm);
        this.appProperties = appProperties;
        this.couponRepository = couponRepository;
        this.couponUsagesRepository = couponUsagesRepository;
        this.geoLocalizationService = geoLocalizationService;
        this.couponsCache = couponsCache;
    }

    public String createCoupon(CreateCouponRequest req) {

        var normalizedCode = req.code().toUpperCase();

        if (!isCountrySupported(req.country())) {
            throw new CountryUnsupportedException();
        }

        var existingCoupon = couponsCache.getCachedCoupon(normalizedCode);
        if (existingCoupon.isPresent()) {
            throw new CouponAlreadyExistsException();
        }

        var createdCoupon = new Coupon();
        createdCoupon.setCode(normalizedCode);
        createdCoupon.setUsagesRemaining(req.usagesMax());
        createdCoupon.setUsagesMax(req.usagesMax());
        createdCoupon.setCountry(req.country());
        couponRepository.save(createdCoupon);

        couponsCache.cacheCoupon(new CachedCoupon(createdCoupon));

        return "Coupon created successfully.";
    }

    public String useCoupon(UseCouponRequest req) {

        var normalizedCode = req.code().toUpperCase();

        var userCountry = geoLocalizationService.getCountryCode(req.userIpAddress());
        if (!isCountrySupported(userCountry)) {
            throw new CountryUnsupportedException();
        }

        var couponInfo = couponsCache.getCachedCoupon(normalizedCode);

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

    private void useCoupon(CachedCoupon cachedCoupon, int userId) {
        transactionTemplate.executeWithoutResult(status -> {
            try {
                couponUsagesRepository.insertUsage(cachedCoupon.getId(), userId);

                var updated = couponRepository.decrementRemaining(cachedCoupon.getCode());
                if (updated == 0) {
                    cachedCoupon.setExhausted(true);
                    couponsCache.cacheCoupon(cachedCoupon);

                    throw new CouponExhaustedException();
                }
            }
            catch (DataIntegrityViolationException e) {
                throw new CouponAlreadyUsedException();
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
