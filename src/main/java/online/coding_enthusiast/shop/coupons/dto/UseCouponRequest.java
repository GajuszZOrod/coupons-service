package online.coding_enthusiast.shop.coupons.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UseCouponRequest(@NotEmpty String code, @NotNull Integer userId, @NotEmpty String userIpAddress) {}
// TODO: define @Pattern for IPv4/IPv6