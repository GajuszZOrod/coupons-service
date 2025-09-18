package online.coding_enthusiast.shop.coupons.helpers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractRestTester<T extends AbstractRestTester<T, U>, U> {

    protected abstract String getUri();

    protected String responseMsg;
    protected HttpStatus responseStatus;

    private static RestClient client;

    public static void initialize(int port) {
        client = RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    @SuppressWarnings("unchecked")
    public T send(U request) {
        if (client == null) {
            throw new IllegalStateException("You must initialize the class and provide the port.");
        }
        try {
            this.responseMsg = client.post()
                    .uri(getUri())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);
            this.responseStatus = HttpStatus.OK;
        }
        catch (HttpStatusCodeException e) {
            this.responseStatus = HttpStatus.valueOf(e.getStatusCode().value());
            this.responseMsg = e.getResponseBodyAsString();
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T expect(String body) {
        assertThat(this.responseMsg).isEqualTo(body);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T expect(HttpStatus code) {
        assertThat(this.responseStatus).isEqualTo(code);
        return (T) this;
    }

    public T expectStatusOk() {
        return expect(HttpStatus.OK);
    }

    public T expectStatusConflict() {
        return expect(HttpStatus.CONFLICT);
    }
}