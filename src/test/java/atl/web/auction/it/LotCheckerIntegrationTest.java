package atl.web.auction.it;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import atl.web.auction.dto.auction.lots.LotFullResponse;
import atl.web.auction.dto.auction.lots.LotRequest;
import atl.web.auction.model.Category;
import atl.web.auction.model.Credential;
import atl.web.auction.model.Lot;
import atl.web.auction.model.User;
import atl.web.auction.model.util.LotStatus;
import atl.web.auction.model.util.Role;
import atl.web.auction.repositories.CategoryRepository;
import atl.web.auction.repositories.LotRepository;
import atl.web.auction.repositories.UserRepository;
import atl.web.auction.services.LotService;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableScheduling
public class LotCheckerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private LotService lotService;

    @Autowired
    private CategoryRepository categoryRepository;

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.hibernate.show-sql", () -> "true");

        registry.add("auction.min-time", () -> "1");
        registry.add("auction.max-time", () -> "10000000");
    }

    @AfterAll
    static void closeContainer() {
        postgreSQLContainer.close();
    }

    @AfterEach
    void clearDB() {
        lotRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private void createUser() {
        String password = "password";
        Credential credential = new Credential(
                null,
                "email@gmail.com",
                encoder.encode(password),
                Role.ROLE_USER,
                null,
                null);

        User user = new User(
                null,
                credential,
                "firstname",
                "lastname",
                null,
                null,
                null,
                null);

        credential.setUser(user);

        userRepository.save(user);
    }

    private Category createCategory() {
        return categoryRepository.save(new Category(null, "Title"));
    }

}
