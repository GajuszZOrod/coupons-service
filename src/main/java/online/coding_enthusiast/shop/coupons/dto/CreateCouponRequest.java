package online.coding_enthusiast.shop.coupons.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateCouponRequest(@NotEmpty String code, @NotNull Integer usagesMax, @NotNull String country) {}
// TODO: define @Pattern for country
