    package com.innowise.orderservice.integration;

    import com.github.tomakehurst.wiremock.WireMockServer;
    import com.github.tomakehurst.wiremock.client.WireMock;
    import com.jayway.jsonpath.JsonPath;
    import org.junit.jupiter.api.AfterAll;
    import org.junit.jupiter.api.BeforeAll;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.http.MediaType;
    import org.springframework.jdbc.core.JdbcTemplate;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.web.servlet.MockMvc;
    import org.springframework.test.web.servlet.MvcResult;
    import org.springframework.test.web.servlet.setup.MockMvcBuilders;
    import org.springframework.web.context.WebApplicationContext;
    import org.testcontainers.junit.jupiter.Testcontainers;

    import static com.github.tomakehurst.wiremock.client.WireMock.*;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @SpringBootTest
    @Testcontainers
    @ActiveProfiles("test")
    public class OrderIntegrationTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        private MockMvc mockMvc;

        private static WireMockServer wireMockServer;

        @BeforeAll
        static void startWireMock() {
            wireMockServer = new WireMockServer(8089);
            wireMockServer.start();

            configureFor("localhost", 8089);
        }

        @AfterAll
        static void stopWireMock() {
            if (wireMockServer != null) {
                wireMockServer.stop();
            }
        }

        @BeforeEach
        void setUp() {

            mockMvc = MockMvcBuilders
                    .webAppContextSetup(webApplicationContext)
                    .build();

            wireMockServer.resetAll();

            stubFor(WireMock.get(urlMatching("/api/users/.*"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withStatus(200)
                            .withBody("""
                {
                  "id": 1,
                  "email": "test@example.com",
                  "name": "John",
                  "surname": "Doe"
                }
            """)));

            jdbcTemplate.execute("""
            TRUNCATE TABLE order_items, orders, items RESTART IDENTITY CASCADE;
        """);

            jdbcTemplate.execute("""
            INSERT INTO items (id, name, price, created_at, updated_at)
            VALUES (1, 'Phone', 1000, now(), now());
        """);
        }

        @Test
        void createOrderTest() throws Exception {

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userId": 1,
                              "items": [
                                {
                                  "itemId": 1,
                                  "quantity": 2
                                }
                              ]
                            }
                        """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderOutputDto.userId").value(1))
                    .andExpect(jsonPath("$.userDto").exists());
        }

        @Test
        void getOrderByIdTest() throws Exception {

            MvcResult result = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userId": 1,
                              "items": [
                                {
                                  "itemId": 1,
                                  "quantity": 1
                                }
                              ]
                            }
                        """))
                    .andExpect(status().isCreated())
                    .andReturn();

            Integer id = JsonPath.read(
                    result.getResponse().getContentAsString(),
                    "$.orderOutputDto.id"
            );

            mockMvc.perform(get("/api/orders/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderOutputDto.id").value(id))
                    .andExpect(jsonPath("$.userDto").exists());
        }

        @Test
        void getOrdersByUserIdTest() throws Exception {

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userId": 10,
                              "items": [
                                {
                                  "itemId": 1,
                                  "quantity": 1
                                }
                              ]
                            }
                        """))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/orders/user/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].orderOutputDto").exists())
                    .andExpect(jsonPath("$[0].userDto").exists());
        }

        @Test
        void updateOrderStatusTest() throws Exception {

            MvcResult result = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userId": 1,
                              "items": [
                                {
                                  "itemId": 1,
                                  "quantity": 1
                                }
                              ]
                            }
                        """))
                    .andExpect(status().isCreated())
                    .andReturn();

            Integer id = JsonPath.read(
                    result.getResponse().getContentAsString(),
                    "$.orderOutputDto.id"
            );

            mockMvc.perform(patch("/api/orders/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "status": "PAID"
                            }
                        """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderOutputDto.status").value("PAID"))
                    .andExpect(jsonPath("$.userDto").exists());
        }

        @Test
        void deleteOrderTest() throws Exception {

            MvcResult result = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "userId": 1,
                              "items": [
                                {
                                  "itemId": 1,
                                  "quantity": 1
                                }
                              ]
                            }
                        """))
                    .andExpect(status().isCreated())
                    .andReturn();

            Integer id = JsonPath.read(
                    result.getResponse().getContentAsString(),
                    "$.orderOutputDto.id"
            );

            mockMvc.perform(delete("/api/orders/" + id))
                    .andExpect(status().isNoContent());
        }
    }