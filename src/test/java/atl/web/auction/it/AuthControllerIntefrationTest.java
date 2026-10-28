package atl.web.auction.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import atl.web.auction.dto.auth.AuthRequest;
import atl.web.auction.dto.auth.AuthResponse;
import atl.web.auction.dto.auth.RegistrationRequest;
import atl.web.auction.dto.auth.RegistrationResponse;
import atl.web.auction.model.Credential;
import atl.web.auction.model.User;
import atl.web.auction.model.util.Role;
import atl.web.auction.repositories.UserRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerIntefrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

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
    }

    @AfterAll
    static void closeContainer() {
        postgreSQLContainer.close();
    }

    @AfterEach
    public void clearDB(){
        userRepository.deleteAll();
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

    @Test
    public void givenAuthRequest_whenUserExistsAndPasswordIsRight_thenReturnAuthData() throws Exception {
        // Given
        createUser();
        AuthRequest request = new AuthRequest("email@gmail.com", "password");

        // When | Then
        String login = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(login, AuthResponse.class);

        assertNotNull(authResponse.getAccessToken());
        assertFalse(authResponse.getAccessToken().isEmpty());
        assertNotNull(authResponse.getRefreshToken());
        assertFalse(authResponse.getRefreshToken().isEmpty());
    }

    @Test
    public void givenAuthRequest_whenUserExistsAndPasswordIsIncorrect_thenReturnException() throws Exception {
        // Given
        createUser();
        AuthRequest request = new AuthRequest("email@gmail.com", "wrong_password");

        // When | Then
        String login = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("Incorrect password or email. Check your data again", login);
    }

    @Test
    public void givenRegistrationRequest_whenEmailDoesntExists_thenReturnRegistrationResponse() throws Exception{
        // Given
        RegistrationRequest request = new RegistrationRequest("email@gmail.com", "password", "firstname", "lastname");

        // When | Then
        String register = mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
        
        RegistrationResponse response = objectMapper.readValue(register, RegistrationResponse.class);
        assertNotNull(response.getId());
        assertEquals("email@gmail.com", response.getEmail());
        assertEquals("firstname", response.getFirstname());
        assertEquals("lastname", response.getLastname());
        assertEquals(Role.ROLE_USER, response.getRole());
    }

    @Test
    public void givenRegistrationRequest_whenEmailExists_thenReturnException() throws Exception{
        // Given
        createUser();
        RegistrationRequest request = new RegistrationRequest("email@gmail.com", "password", "firstname", "lastname");

        // When | Then
        String register = mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
        
        assertEquals("Email 'email@gmail.com' already exists in system", register);
    }
}
