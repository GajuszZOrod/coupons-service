package online.coding_enthusiast.shop.coupons.helpers;

import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractMysqlRedisWiremockTest extends AbstractMysqlTest {

    @LocalServerPort
    protected int port;

    @BeforeAll
    static void init(@LocalServerPort int port) {
        AbstractRestTester.initialize(port);
    }

    private static class RedisContainerSingleton {
        @Getter
        private static final GenericContainer<?> instance = new GenericContainer<>("redis:8.2.1")
                .withExposedPorts(6379);
        static
        {
            instance.start();
        }
    }

    private static class WiremockContainerSingleton {
        @Getter
        private static final GenericContainer<?> instance = new GenericContainer<>("wiremock/wiremock:3.13.1-1")
                .withExposedPorts(8080)
                .withClasspathResourceMapping("wiremock-stubs", "/home/wiremock/mappings", BindMode.READ_ONLY);
        static
        {
            instance.start();
        }
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        var redis = RedisContainerSingleton.getInstance();
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.jpa.show-sql", () -> "true");

        var wiremock = WiremockContainerSingleton.getInstance();
        var url = "http://" + wiremock.getHost() + ":" + wiremock.getMappedPort(8080);
        registry.add("coupons.ipWhoIsUrl", () -> url);
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    protected void clearCache() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb(RedisServerCommands.FlushOption.SYNC);
    }

}
