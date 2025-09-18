package online.coding_enthusiast.shop.coupons.helpers;

import online.coding_enthusiast.shop.coupons.dto.CreateCouponRequest;

public class CreateCouponTester extends AbstractRestTester<CreateCouponTester, CreateCouponRequest> {

    @Override
    protected String getUri() {
        return "/v1/coupons";
    }
}
