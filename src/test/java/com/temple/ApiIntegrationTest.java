package com.temple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.temple.entity.Admin;
import com.temple.repository.AdminRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end API tests against PostgreSQL. MockMvc paths omit the /api
 * servlet context path.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
class ApiIntegrationTest {

    private static final byte[] PNG = {
        (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D, 'I', 'H', 'D', 'R'
    };

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AdminRepository admins;

    String token;

    @BeforeEach
    void login() throws Exception {
        token = loginAs("admin", "test-admin-password");
    }

    private String loginAs(String username, String password) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private String bearer() {
        return "Bearer " + token;
    }

    private static String eventJson(String title, LocalDate date) {
        return """
                {"title":"%s","eventDate":"%s","startTime":"18:00:00","endTime":"20:00:00",
                 "location":"Main Hall","category":"Festival"}""".formatted(title, date);
    }

    private long createEvent(String title, LocalDate date) throws Exception {
        String body = mvc.perform(post("/events").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(eventJson(title, date)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    @Test
    void writesRequireAValidAdminToken() throws Exception {
        mvc.perform(get("/events")).andExpect(status().isOk());

        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(eventJson("X", LocalDate.now())))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        // The old interceptor accepted ANY "Bearer ..." header. A forged token must now be rejected.
        mvc.perform(post("/events").header(HttpHeaders.AUTHORIZATION, "Bearer forged.token.value")
                        .contentType(MediaType.APPLICATION_JSON).content(eventJson("X", LocalDate.now())))
                .andExpect(status().isUnauthorized());

        mvc.perform(delete("/services/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/subscriptions/admin/stats")).andExpect(status().isUnauthorized());
        mvc.perform(get("/donors/admin/all")).andExpect(status().isUnauthorized());
    }

    @Test
    void dataWipeEndpointsNoLongerExist() throws Exception {
        mvc.perform(post("/clear-data").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/init-data").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void staleTokenDoesNotBreakPublicPages() throws Exception {
        mvc.perform(get("/events").header(HttpHeaders.AUTHORIZATION, "Bearer expired.or.bad"))
                .andExpect(status().isOk());
    }

    @Test
    void badCredentialsGetAGenericMessageAndAreThrottled() throws Exception {
        String bad = "{\"username\":\"nobody\",\"password\":\"wrong-password\"}";
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(bad))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid username or password"));
        }
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void legacyPlaintextPasswordIsUpgradedOnFirstLogin() throws Exception {
        admins.save(Admin.builder().username("legacy").password("old-plain-pass").role("ADMIN").enabled(true).build());

        loginAs("legacy", "old-plain-pass");

        String stored = admins.findByUsername("legacy").orElseThrow().getPassword();
        assertThat(stored).startsWith("{bcrypt}");
        loginAs("legacy", "old-plain-pass");
    }

    @Test
    void eventCrudKeepsPhotoOnJsonUpdate() throws Exception {
        mvc.perform(post("/events").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.eventName").exists());

        MockMultipartFile event = new MockMultipartFile("event", "", MediaType.APPLICATION_JSON_VALUE,
                eventJson("Ugadi", LocalDate.now().plusDays(5)).getBytes());
        MockMultipartFile photo = new MockMultipartFile("photo", "photo.png", "image/png", PNG);
        String created = mvc.perform(multipart("/events").file(event).file(photo)
                        .header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Ugadi"))
                .andExpect(jsonPath("$.imageUrl").value(startsWith("/api/photos/")))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(created);
        long id = node.get("id").asLong();
        String imageUrl = node.get("imageUrl").asText();

        mvc.perform(get(imageUrl.replaceFirst("^/api", "")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));

        mvc.perform(put("/events/" + id).header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson("Ugadi Celebrations", LocalDate.now().plusDays(5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Ugadi Celebrations"))
                .andExpect(jsonPath("$.imageUrl").value(imageUrl));

        mvc.perform(delete("/events/" + id).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/events/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsScriptsDisguisedAsImages() throws Exception {
        MockMultipartFile event = new MockMultipartFile("event", "", MediaType.APPLICATION_JSON_VALUE,
                eventJson("Bad", LocalDate.now().plusDays(5)).getBytes());
        MockMultipartFile svg = new MockMultipartFile("photo", "x.png", "image/png",
                "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"/>".getBytes());
        mvc.perform(multipart("/events").file(event).file(svg).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upcomingFilterAndPastEventsAreKept() throws Exception {
        createEvent("Past festival", LocalDate.now().minusDays(30));
        createEvent("Future festival", LocalDate.now().plusDays(30));

        String upcoming = mvc.perform(get("/events").param("upcoming", "true"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(upcoming).contains("Future festival").doesNotContain("Past festival");

        String all = mvc.perform(get("/events")).andReturn().getResponse().getContentAsString();
        assertThat(all).contains("Past festival");
    }

    @Test
    void registrationFlowAndDeletingAnEventWithRegistrations() throws Exception {
        long eventId = createEvent("Navaratri", LocalDate.now().plusDays(10));

        mvc.perform(post("/events/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"eventId": %d, "members": [
                          {"name": "Ravi", "surname": "K", "email": "ravi@example.org", "phoneNo": "+91 98765 43210"},
                          {"name": "Lakshmi", "email": "", "phoneNo": ""}
                        ]}""".formatted(eventId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.members", hasSize(2)));

        mvc.perform(post("/events/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\": %d, \"members\": [{\"name\": \"\", \"email\": \"nope\"}]}".formatted(eventId)))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/events/" + eventId + "/registrations")).andExpect(status().isUnauthorized());
        mvc.perform(get("/events/" + eventId + "/registration-stats").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRegistrations").value(1))
                .andExpect(jsonPath("$.totalMembers").value(2));

        mvc.perform(delete("/events/" + eventId).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isNoContent());
    }

    @Test
    void donorListIsPublicButHidesContactDetails() throws Exception {
        for (int i = 0; i < 2; i++) {   // same email twice: repeat donors are allowed now
            mvc.perform(post("/donors").header(HttpHeaders.AUTHORIZATION, bearer())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"donorName\":\"Anand\",\"donationType\":\"Cash\",\"amount\":1001,"
                                    + "\"donationDate\":\"2026-01-14\",\"email\":\"anand@example.org\",\"address\":\"1 Temple St\"}"))
                    .andExpect(status().isCreated());
        }
        mvc.perform(get("/donors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].donorName").value("Anand"))
                .andExpect(jsonPath("$[0].amount").isNumber())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].address").doesNotExist());

        mvc.perform(post("/donors").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"donorName\":\"X\",\"donationType\":\"Cash\",\"amount\":-5}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/donors/statistics")).andExpect(status().isOk())
                .andExpect(jsonPath("$.donationCount").isNumber());
    }

    @Test
    void timingsSessionsAreValidatedAndSorted() throws Exception {
        mvc.perform(post("/timings").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dayOfWeek\":\"tuesday\",\"openingTime\":\"16:30:00\",\"closingTime\":\"20:30:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("Tuesday"));
        mvc.perform(post("/timings").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dayOfWeek\":\"Monday\",\"openingTime\":\"08:30:00\",\"closingTime\":\"11:30:00\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/timings").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dayOfWeek\":\"Funday\",\"openingTime\":\"20:00:00\",\"closingTime\":\"08:00:00\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/timings")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dayOfWeek").value("Monday"));
    }

    @Test
    void staffUpdateKeepsPhotoAndAcceptsUiFieldNames() throws Exception {
        String created = mvc.perform(post("/staff").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sri Ram\",\"position\":\"Priest\",\"department\":\"Religious\","
                                + "\"phone\":\"9876543210\",\"bio\":\"30 years\",\"profileImageUrl\":\"/images/ram.jpg\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Sri Ram"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"))
                .andExpect(jsonPath("$.biography").value("30 years"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(put("/staff/" + id).header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Sri Ram Sharma\",\"department\":\"Religious\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileImageUrl").value("/images/ram.jpg"));

        mvc.perform(get("/staff").param("department", "religious")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Sri Ram Sharma"));

        mvc.perform(post("/staff").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"X\",\"profileImageUrl\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void newsletterSubscribeIsIdempotentAndDoesNotLeakSubscribers() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/subscriptions/subscribe").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"Devotee@Example.org\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("devotee@example.org"));
        }
        mvc.perform(post("/subscriptions/unsubscribe").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"never-subscribed@example.org\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/subscriptions/devotee@example.org")).andExpect(status().isUnauthorized());

        mvc.perform(get("/subscriptions/admin/all").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("devotee@example.org"))
                .andExpect(jsonPath("$[0].unsubscribeToken").doesNotExist());

        mvc.perform(post("/subscriptions/admin/send-test").header(HttpHeaders.AUTHORIZATION, bearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@example.org\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void adminAccountsNeverExposePasswords() throws Exception {
        mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
