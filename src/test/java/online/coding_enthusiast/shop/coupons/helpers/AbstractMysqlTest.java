package online.coding_enthusiast.shop.coupons.helpers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractMysqlTest {

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        var container = MySQLContainerSingleton.getInstance();
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
    }

    private static class MySQLContainerSingleton {
        @Getter
        private static final MySQLContainer<?> instance = new MySQLContainer<>("mysql:9.2")
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("test");
        static
        {
            instance.start();
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    PlatformTransactionManager transactionManager;

    protected void clearDatabase() {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.executeWithoutResult(status -> {
            entityManager.createQuery("DELETE FROM CouponUsage").executeUpdate();
            entityManager.createQuery("DELETE FROM Coupon").executeUpdate();
        });
    }
}
