package io.life.user_service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.life.user_service.dto.auth.LoginRequest;
import io.life.user_service.dto.user.UserUpdateRequest;
import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;
import io.life.user_service.repository.UserRepository;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

@SpringBootTest
class UserControllerIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void currentUserEndpointReturnsAuthenticatedUser() throws Exception {
        userRepository.save(new User("eve", passwordEncoder.encode("password1"), UserRole.VIEWER, 10L));

        String token = loginAndExtractToken("eve", "password1");

        mockMvc.perform(get("/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("eve"))
            .andExpect(jsonPath("$.workstationId").value(10));
    }

    @Test
    void listUsersRequiresAdminAndReturnsAllUsers() throws Exception {
        userRepository.save(new User("admin", passwordEncoder.encode("adminPass1"), UserRole.ADMIN, null));
        userRepository.save(new User("viewer", passwordEncoder.encode("viewerPass1"), UserRole.VIEWER, null));

        String adminToken = loginAndExtractToken("admin", "adminPass1");

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].username").isArray())
            .andExpect(jsonPath("$.[*].username").isNotEmpty());
    }

    @Test
    void getUserByIdReturnsUserDetailsForAdmin() throws Exception {
        User admin = userRepository.save(new User("admin", passwordEncoder.encode("adminPass1"), UserRole.ADMIN, null));
        User operator = userRepository.save(new User("operator", passwordEncoder.encode("operator1"), UserRole.PARTS_SUPPLY, 12L));

        String adminToken = loginAndExtractToken(admin.getUsername(), "adminPass1");

        mockMvc.perform(get("/api/users/" + operator.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("operator"))
            .andExpect(jsonPath("$.role").value("PARTS_SUPPLY"));
    }

    @Test
    void updateUserAllowsAdminToChangeRoleAndPassword() throws Exception {
        userRepository.save(new User("admin", passwordEncoder.encode("adminPass1"), UserRole.ADMIN, null));
        User target = userRepository.save(new User("operator", passwordEncoder.encode("operator1"), UserRole.PARTS_SUPPLY, 12L));

        String adminToken = loginAndExtractToken("admin", "adminPass1");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("operator");
        request.setPassword("newOperator1");
        request.setRole(UserRole.PRODUCTION_CONTROL);
        request.setWorkstationId(42L);

        mockMvc.perform(put("/api/users/" + target.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("PRODUCTION_CONTROL"))
            .andExpect(jsonPath("$.workstationId").value(42));

        // ensure password update took effect by logging in with new password
        String operatorToken = loginAndExtractToken("operator", "newOperator1");
        assertThat(operatorToken).isNotBlank();
    }

    @Test
    void deleteUserRemovesAccount() throws Exception {
        userRepository.save(new User("admin", passwordEncoder.encode("adminPass1"), UserRole.ADMIN, null));
        User removable = userRepository.save(new User("temp-user", passwordEncoder.encode("tempPass1"), UserRole.VIEWER, null));

        String adminToken = loginAndExtractToken("admin", "adminPass1");

        mockMvc.perform(delete("/api/users/" + removable.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
            .andExpect(status().isNoContent());

        assertThat(userRepository.findById(removable.getId())).isEmpty();
    }

    private String loginAndExtractToken(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}
