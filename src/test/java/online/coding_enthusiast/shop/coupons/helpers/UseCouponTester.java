package online.coding_enthusiast.shop.coupons.helpers;

import online.coding_enthusiast.shop.coupons.dto.UseCouponRequest;

public class UseCouponTester extends AbstractRestTester<UseCouponTester, UseCouponRequest> {

    @Override
    protected String getUri() {
        return "/v1/coupons-usages";
    }
}
