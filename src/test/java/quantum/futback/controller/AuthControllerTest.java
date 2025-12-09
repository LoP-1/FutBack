package quantum.futback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import quantum.futback.core.multitenancy.TenantContext;
import quantum.futback.entity.DTO.LoginRequest;
import quantum.futback.entity.Role;
import quantum.futback.entity.Tenant;
import quantum.futback.entity.User;
import quantum.futback.repository.UserRepository;

import jakarta.persistence.EntityManager;
import java.util.UUID; // <--- ¡Importación necesaria!

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Tenant testTenant;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Create test tenant first
        testTenant = new Tenant();
        testTenant.setName("Test Tenant");
        testTenant.setActive(true);
        entityManager.persist(testTenant);
        entityManager.flush();

        // Set the actual tenant ID in context
        TenantContext.setTenantId(testTenant.getId());

        // Testear un rol
        testRole = new Role();
        testRole.setTenantId(testTenant.getId());
        testRole.setName("ROLE_USER");
        entityManager.persist(testRole);

        // Testear al usuario
        testUser = new User();
        testUser.setTenantId(testTenant.getId());
        testUser.setRole(testRole);
        testUser.setDni("12345678");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setFullName("Test User");
        testUser.setActive(true);
        entityManager.persist(testUser);

        entityManager.flush();
    }

    // Limpiamos el contexto después de cada test para no afectar a otros
    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void login_WithValidCredentials_ReturnsAccessTokenAndRefreshToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.roleName", is("ROLE_USER")));
    }

    @Test
    void login_WithInvalidCredentials_ReturnsUnauthorizedOrForbidden() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_Returns401Or403() throws Exception {
        mockMvc.perform(get("/api/players"))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    void refreshToken_WithValidToken_ReturnsNewTokens() throws Exception {
        // First, login to get tokens
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(responseContent).get("refreshToken").asText();

        // Now use the refresh token to get new tokens
        String refreshRequest = "{\"token\": \"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void logout_WithValidToken_InvalidatesToken() throws Exception {
        // First, login to get tokens
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(responseContent).get("refreshToken").asText();

        // Logout with the refresh token
        String logoutRequest = "{\"token\": \"" + refreshToken + "\"}";

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutRequest))
                .andExpect(status().isOk());

        // Try to use the invalidated refresh token
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithValidToken_ReturnsSuccess() throws Exception {
        // First, login to get tokens
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseContent).get("accessToken").asText();

        // Access a public endpoint with token (should work)
        mockMvc.perform(get("/api/tenants/current")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }
}