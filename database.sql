
CREATE TABLE `coupons` (
  `id` integer PRIMARY KEY AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8_general_ci UNIQUE NOT NULL,
  `usages_max` integer NOT NULL,
  `usages_remaining` integer NOT NULL,
  `country` char(2) NOT NULL,
  `created_at` date
);

CREATE TABLE `coupon_usages` (
  `coupon_id` integer NOT NULL,
  `user_id` integer NOT NULL,
  `used_at` datetime,
  PRIMARY KEY (`coupon_id`, `user_id`)
);

CREATE INDEX `coupons_index_0` ON `coupons` (`code`);

CREATE INDEX `coupon_usages_index_1` ON `coupon_usages` (`user_id`);

CREATE INDEX `coupon_usages_index_2` ON `coupon_usages` (`coupon_id`);

ALTER TABLE `coupon_usages` ADD FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`);