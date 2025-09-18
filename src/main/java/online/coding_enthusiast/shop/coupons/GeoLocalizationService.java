package online.coding_enthusiast.shop.coupons;

import online.coding_enthusiast.shop.coupons.exceptions.UndeterminedUserCountryException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GeoLocalizationService {

    private final RestClient restClient;

    public GeoLocalizationService(RestClient.Builder builder, AppProperties appProperties) {
        this.restClient = builder.baseUrl(appProperties.getIpWhoIsUrl()).build();
    }

    @Cacheable(value = "countryByIp")
    public String getCountryCode(String ipAddress) {
        var response = restClient.get()
                .uri("/{ipAddress}", ipAddress)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Response.class);

        if (response == null || !response.success()) {
            throw new UndeterminedUserCountryException();
        }

        return response.country_code();
    }

    private record Response(boolean success, String country_code) {}
}
