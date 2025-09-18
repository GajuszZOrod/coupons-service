package online.coding_enthusiast.shop.coupons;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "coupons")
@Data
public class AppProperties {

    private List<String> supportedCountries = List.of();

    private String ipWhoIsUrl = "https://ipwho.is";
}
