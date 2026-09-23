package com.inventoryhub;

import com.inventoryhub.catalog.Product;
import com.inventoryhub.catalog.ProductRepository;
import com.inventoryhub.events.EventOutboxRepository;
import com.inventoryhub.events.EventProcessingRecordRepository;
import com.inventoryhub.inventory.InventoryItem;
import com.inventoryhub.inventory.InventoryRepository;
import com.inventoryhub.inventory.ReservationRepository;
import com.inventoryhub.notifications.NotificationAttemptRepository;
import com.inventoryhub.orders.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryHubApplicationTests {
    private static final UUID COFFEE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MUG_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INACTIVE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EventOutboxRepository eventOutboxRepository;

    @Autowired
    private EventProcessingRecordRepository eventProcessingRecordRepository;

    @Autowired
    private NotificationAttemptRepository notificationAttemptRepository;

    @BeforeEach
    void setUp() {
        notificationAttemptRepository.deleteAll();
        eventProcessingRecordRepository.deleteAll();
        eventOutboxRepository.deleteAll();
        reservationRepository.deleteAll();
        orderRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();

        productRepository.save(new Product(COFFEE_ID, "SKU-COFFEE", "Whole Bean Coffee", "Medium roast", new BigDecimal("14.99"), true));
        productRepository.save(new Product(MUG_ID, "SKU-MUG", "Ceramic Mug", "Demo mug", new BigDecimal("9.99"), true));
        productRepository.save(new Product(INACTIVE_ID, "SKU-OLD", "Inactive Product", "Not available", new BigDecimal("1.99"), false));

        inventoryRepository.save(new InventoryItem(UUID.randomUUID(), COFFEE_ID, 25));
        inventoryRepository.save(new InventoryItem(UUID.randomUUID(), MUG_ID, 1));
        inventoryRepository.save(new InventoryItem(UUID.randomUUID(), INACTIVE_ID, 5));
    }

    @Test
    void listsActiveProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].availabilitySummary", notNullValue()));
    }

    @Test
    void createsOrderAndConfirmsAfterInventoryReservation() throws Exception {
        String body = """
                {
                  "customerEmail": "customer@example.com",
                  "items": [
                    {"productId": "%s", "quantity": 2}
                  ]
                }
                """.formatted(COFFEE_ID);

        String response = mockMvc.perform(post("/api/orders")
                        .header("X-Correlation-Id", "test-correlation")
                        .header("Idempotency-Key", "order-key-1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Correlation-Id", "test-correlation"))
                .andExpect(jsonPath("$.status", is("PENDING_RESERVATION")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = response.replaceAll(".*\"orderId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.totalAmount", is(29.98)));

        mockMvc.perform(get("/api/inventory/{productId}", COFFEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity", is(23)))
                .andExpect(jsonPath("$.reservedQuantity", is(2)));

        org.assertj.core.api.Assertions.assertThat(notificationAttemptRepository.count()).isEqualTo(1);
    }

    @Test
    void cancelsOrderWhenInventoryIsInsufficient() throws Exception {
        String body = """
                {
                  "customerEmail": "customer@example.com",
                  "items": [
                    {"productId": "%s", "quantity": 2}
                  ]
                }
                """.formatted(MUG_ID);

        String response = mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = response.replaceAll(".*\"orderId\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$.failureReason", is("INSUFFICIENT_STOCK")));

        mockMvc.perform(get("/api/inventory/{productId}", MUG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity", is(1)))
                .andExpect(jsonPath("$.reservedQuantity", is(0)));
    }

    @Test
    void rejectsInactiveProducts() throws Exception {
        String body = """
                {
                  "customerEmail": "customer@example.com",
                  "items": [
                    {"productId": "%s", "quantity": 1}
                  ]
                }
                """.formatted(INACTIVE_ID);

        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    void idempotencyKeyReturnsExistingOrder() throws Exception {
        String body = """
                {
                  "customerEmail": "customer@example.com",
                  "items": [
                    {"productId": "%s", "quantity": 1}
                  ]
                }
                """.formatted(COFFEE_ID);

        String first = mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "repeat-key")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String second = mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "repeat-key")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String firstOrderId = first.replaceAll(".*\"orderId\":\"([^\"]+)\".*", "$1");
        String secondOrderId = second.replaceAll(".*\"orderId\":\"([^\"]+)\".*", "$1");

        org.assertj.core.api.Assertions.assertThat(secondOrderId).isEqualTo(firstOrderId);
        org.assertj.core.api.Assertions.assertThat(orderRepository.count()).isEqualTo(1);
    }
}
