package com.innowise.orderservice.integration;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.innowise.orderservice.dto.output.OrderOutputDto;
import com.jayway.jsonpath.JsonPath;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class OrderIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.webApplicationContext)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


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
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items").isArray());
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

        Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        Long id = idNumber.longValue();

        mockMvc.perform(get("/api/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
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
                .andExpect(jsonPath("$").isArray());
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

        Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        Long id = idNumber.longValue();

        // Вариант 1: Отправить как JSON в теле запроса
        mockMvc.perform(patch("/api/orders/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "PAID"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
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

        Number idNumber = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        Long id = idNumber.longValue();

        mockMvc.perform(delete("/api/orders/" + id))
                .andExpect(status().isNoContent());
    }
}
