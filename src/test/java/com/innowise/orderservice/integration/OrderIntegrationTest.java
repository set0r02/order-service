    package com.innowise.orderservice.integration;

    import com.innowise.orderservice.client.UserServiceClient;
    import com.innowise.orderservice.dto.output.UserDto;
    import com.jayway.jsonpath.JsonPath;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.http.MediaType;
    import org.springframework.jdbc.core.JdbcTemplate;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.servlet.MockMvc;
    import org.springframework.test.web.servlet.MvcResult;
    import org.springframework.test.web.servlet.setup.MockMvcBuilders;
    import org.springframework.web.context.WebApplicationContext;
    import org.testcontainers.junit.jupiter.Testcontainers;

    import static org.mockito.ArgumentMatchers.anyLong;
    import static org.mockito.Mockito.when;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @SpringBootTest
    @Testcontainers
    @ActiveProfiles("test")
    public class OrderIntegrationTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mockMvc;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @MockitoBean
        private UserServiceClient userServiceClient;

        @BeforeEach
        void setUp() {

            this.mockMvc = MockMvcBuilders
                    .webAppContextSetup(webApplicationContext)
                    .build();

            // Мокаем ответ от User Service для всех userId
            UserDto mockUser = new UserDto(1L, "test1@example.com", "John", "Doe");
            UserDto mockUser10 = new UserDto(10L, "test10@example.com", "Jane", "Smith");

            when(userServiceClient.getUserById(1L)).thenReturn(mockUser);
            when(userServiceClient.getUserById(10L)).thenReturn(mockUser10);
            when(userServiceClient.getUserById(anyLong())).thenReturn(mockUser);

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
                    .andExpect(jsonPath("$.orderOutputDto.items").isArray())
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

            Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.orderOutputDto.id");
            Long id = idNumber.longValue();

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
                    .andExpect(jsonPath("$").isArray())
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

            Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.orderOutputDto.id");
            Long id = idNumber.longValue();

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

            Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.orderOutputDto.id");
            Long id = idNumber.longValue();

            mockMvc.perform(delete("/api/orders/" + id))
                    .andExpect(status().isNoContent());
        }
    }