package online.coding_enthusiast.shop.coupons;

import jakarta.validation.Valid;
import online.coding_enthusiast.shop.coupons.dto.CreateCouponRequest;
import online.coding_enthusiast.shop.coupons.dto.UseCouponRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class CouponsController {

    @Autowired
    CouponsService couponsService;

    // TODO: some endpoint for viewing/cleaning coupon_usages list?
    // TODO: some endpoint for cleaning old exhausted coupons?

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody @Valid CreateCouponRequest req) {
        return ResponseEntity.ok(couponsService.createCoupon(req));
    }

    @PostMapping("/coupons-usages")
    public ResponseEntity<?> useCoupon(@RequestBody @Valid UseCouponRequest req) {
        return ResponseEntity.ok(couponsService.useCoupon(req));
    }

}
