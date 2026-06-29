// package com.app.demo.security;

// import static org.hamcrest.Matchers.containsString;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

// import java.util.Map;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.web.context.WebApplicationContext;

// import com.app.demo.SpringSecurityApplication;
// import com.app.demo.dto.TokenRefreshRequest;
// import com.app.demo.model.Admin;
// import com.app.demo.model.RefreshToken;
// import com.app.demo.repo.AdminRepository;
// import com.app.demo.repo.AuditLogRepository;
// import com.app.demo.repo.BlacklistedTokenRepository;
// import com.app.demo.repo.RefreshTokenRepository;
// import com.fasterxml.jackson.databind.ObjectMapper;

// @SpringBootTest(classes = SpringSecurityApplication.class)
// @ActiveProfiles("test")
// public class SecurityFlowIntegrationTests {

//     private MockMvc mockMvc;
//     private ObjectMapper objectMapper;

//     @Autowired
//     private WebApplicationContext context;

//     @Autowired
//     private RateLimitingFilter rateLimitingFilter;

//     @Autowired
//     private AdminRepository adminRepository;

//     @Autowired
//     private RefreshTokenRepository refreshTokenRepository;

//     @Autowired
//     private BlacklistedTokenRepository blacklistedTokenRepository;

//     @Autowired
//     private AuditLogRepository auditLogRepository;

//     @Autowired
//     private PasswordEncoder passwordEncoder;

//     @BeforeEach
//     public void setup() {
//         mockMvc = MockMvcBuilders
//                 .webAppContextSetup(context)
//                 .apply(springSecurity())
//                 .build();

//         objectMapper = new ObjectMapper();
//         rateLimitingFilter.clearRateLimits();

//         refreshTokenRepository.deleteAll();
//         blacklistedTokenRepository.deleteAll();
//         adminRepository.deleteAll();
//         auditLogRepository.deleteAll();
//     }

//     @Test
//     public void testAdminRegistrationAndLoginFlow() throws Exception {
//         String adminRegisterJson = "{"
//                 + "\"fullName\":\"John Admin\","
//                 + "\"email\":\"john@example.com\","
//                 + "\"phoneNumber\":\"9876543210\","
//                 + "\"password\":\"SecurePass123\","
//                 + "\"confirmPassword\":\"SecurePass123\""
//                 + "}";

//         mockMvc.perform(post("/auth/Admin/SignUp")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(adminRegisterJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").exists())
//                 .andExpect(jsonPath("$.refreshToken").exists());

//         assertTrue(adminRepository.findByEmail("john@example.com").isPresent());

//         long countRegLogs = auditLogRepository.findAll().stream()
//                 .filter(log -> log.getActivity().contains("registered") && log.getUserType().equals("ADMIN"))
//                 .count();
//         assertEquals(1, countRegLogs);

//         // Clear rate limits before SignIn call
//         rateLimitingFilter.clearRateLimits();

//         String loginJson = "{"
//                 + "\"identifier\":\"john@example.com\","
//                 + "\"password\":\"SecurePass123\""
//                 + "}";

//         mockMvc.perform(post("/auth/Admin/SignIn")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(loginJson))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.data.token").exists())
//                 .andExpect(jsonPath("$.data.refreshToken").exists());

//         long countLoginLogs = auditLogRepository.findAll().stream()
//                 .filter(log -> log.getActivity().contains("logged in") && log.getUserType().equals("ADMIN"))
//                 .count();
//         assertEquals(1, countLoginLogs);
//     }

//     @Test
//     public void testRoleBasedAccessControlAndAuditLogs() throws Exception {
//         Admin admin = new Admin();
//         admin.setFullName("Admin User");
//         admin.setEmail("admin@app.com");
//         admin.setPhoneNumber("1111111111");
//         admin.setPassword(passwordEncoder.encode("adminpass"));
//         admin.setRole("ROLE_ADMIN");
//         adminRepository.save(admin);

//         Admin superAdmin = new Admin();
//         superAdmin.setFullName("Super Admin User");
//         superAdmin.setEmail("superadmin@app.com");
//         superAdmin.setPhoneNumber("2222222222");
//         superAdmin.setPassword(passwordEncoder.encode("superpass"));
//         superAdmin.setRole("ROLE_SUPER_ADMIN");
//         adminRepository.save(superAdmin);

//         String adminLogin = "{\"identifier\":\"admin@app.com\",\"password\":\"adminpass\"}";
//         MvcResult adminLoginRes = mockMvc.perform(post("/auth/Admin/SignIn")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(adminLogin))
//                 .andExpect(status().isOk())
//                 .andReturn();
//         String responseContent = adminLoginRes.getResponse().getContentAsString();
//         Map<?, ?> responseMap = objectMapper.readValue(responseContent, Map.class);
//         Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
//         String adminToken = (String) dataMap.get("token");

//         // Clear rate limits before the next SignIn call
//         rateLimitingFilter.clearRateLimits();

//         String superLogin = "{\"identifier\":\"superadmin@app.com\",\"password\":\"superpass\"}";
//         MvcResult superLoginRes = mockMvc.perform(post("/auth/Admin/SignIn")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(superLogin))
//                 .andExpect(status().isOk())
//                 .andReturn();
//         String superResponseContent = superLoginRes.getResponse().getContentAsString();
//         Map<?, ?> superResponseMap = objectMapper.readValue(superResponseContent, Map.class);
//         Map<?, ?> superDataMap = (Map<?, ?>) superResponseMap.get("data");
//         String superToken = (String) superDataMap.get("token");

//         mockMvc.perform(get("/api/daily-connect/jobs")
//                 .header("Authorization", "Bearer " + adminToken))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message", containsString("successfully")));

//         mockMvc.perform(get("/api/daily-connect/jobs")
//                 .header("Authorization", "Bearer " + superToken))
//                 .andExpect(status().isOk());

//         mockMvc.perform(post("/api/daily-connect/admin/connections")
//                 .header("Authorization", "Bearer " + superToken))
//                 .andExpect(status().isOk());

//         mockMvc.perform(post("/api/daily-connect/admin/connections")
//                 .header("Authorization", "Bearer " + adminToken))
//                 .andExpect(status().isForbidden());

//         long countAccessDeniedLogs = auditLogRepository.findAll().stream()
//                 .filter(log -> log.getActivity().contains("Access Denied"))
//                 .count();
//         assertEquals(1, countAccessDeniedLogs);
//     }

//     @Test
//     public void testTokenRefreshFlow() throws Exception {
//         String adminRegisterJson = "{"
//                 + "\"fullName\":\"Refresh Tester\","
//                 + "\"email\":\"tester@app.com\","
//                 + "\"phoneNumber\":\"9999999999\","
//                 + "\"password\":\"pass123\","
//                 + "\"confirmPassword\":\"pass123\""
//                 + "}";

//         MvcResult res = mockMvc.perform(post("/auth/Admin/SignUp")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(adminRegisterJson))
//                 .andExpect(status().isOk())
//                 .andReturn();

//         String body = res.getResponse().getContentAsString();
//         Map<?, ?> bodyMap = objectMapper.readValue(body, Map.class);
//         String refreshToken = (String) bodyMap.get("refreshToken");

//         // Clear rate limits before the /auth/refresh call
//         rateLimitingFilter.clearRateLimits();

//         TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
//         refreshRequest.setRefreshToken(refreshToken);

//         mockMvc.perform(post("/auth/refresh")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(refreshRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.accessToken").exists())
//                 .andExpect(jsonPath("$.refreshToken").exists());
//     }

//     @Test
//     public void testLogoutAndTokenRevocation() throws Exception {
//         Admin admin = new Admin();
//         admin.setFullName("Logout User");
//         admin.setEmail("logout@app.com");
//         admin.setPhoneNumber("1234567890");
//         admin.setPassword(passwordEncoder.encode("password"));
//         admin.setRole("ROLE_ADMIN");
//         adminRepository.save(admin);

//         String loginJson = "{\"identifier\":\"logout@app.com\",\"password\":\"password\"}";
//         MvcResult loginResult = mockMvc.perform(post("/auth/Admin/SignIn")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(loginJson))
//                 .andExpect(status().isOk())
//                 .andReturn();

//         String responseContent = loginResult.getResponse().getContentAsString();
//         Map<?, ?> responseMap = objectMapper.readValue(responseContent, Map.class);
//         Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
//         String accessToken = (String) dataMap.get("token");
//         String refreshToken = (String) dataMap.get("refreshToken");

//         mockMvc.perform(get("/api/daily-connect/jobs")
//                 .header("Authorization", "Bearer " + accessToken))
//                 .andExpect(status().isOk());

//         Optional<RefreshToken> rtBefore = refreshTokenRepository.findByToken(refreshToken);
//         assertTrue(rtBefore.isPresent());

//         // Clear rate limits before the /auth/logout call
//         rateLimitingFilter.clearRateLimits();

//         mockMvc.perform(post("/auth/logout")
//                 .header("Authorization", "Bearer " + accessToken))
//                 .andExpect(status().isOk());

//         Optional<RefreshToken> rtAfter = refreshTokenRepository.findByToken(refreshToken);
//         assertFalse(rtAfter.isPresent());

//         mockMvc.perform(get("/api/daily-connect/jobs")
//                 .header("Authorization", "Bearer " + accessToken))
//                 .andExpect(status().isUnauthorized());
//     }

//     @Test
//     public void testRateLimitingOnAuthEndpoints() throws Exception {
//         String loginJson = "{\"identifier\":\"ratelimit@app.com\",\"password\":\"password\"}";

//         for (int i = 0; i < 1; i++) {
//             mockMvc.perform(post("/auth/Admin/SignIn")
//                     .contentType(MediaType.APPLICATION_JSON)
//                     .content(loginJson));
//         }

//         mockMvc.perform(post("/auth/Admin/SignIn")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(loginJson))
//                 .andExpect(status().isTooManyRequests())
//                 .andExpect(jsonPath("$.error", containsString("Too many requests")));

//         long countRateLimitLogs = auditLogRepository.findAll().stream()
//                 .filter(log -> log.getActivity().contains("Rate limit exceeded"))
//                 .count();
//         assertEquals(1, countRateLimitLogs);
//     }
// }