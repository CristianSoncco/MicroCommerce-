package com.microcommerce.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microcommerce.order.dto.OrderDTO;
import com.microcommerce.order.dto.OrderItemDTO;
import com.microcommerce.order.dto.response.OrderResponse;
import com.microcommerce.order.dto.response.OrderResponse.OrderItemResponse;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.entity.OrderItem;
import com.microcommerce.order.exception.EmptyOrderException;
import com.microcommerce.order.exception.InvalidOrderStatusException;
import com.microcommerce.order.exception.OrderNotFoundException;
import com.microcommerce.order.exception.handler.GlobalExceptionHandler;
import com.microcommerce.order.mapper.OrderMapper;
import com.microcommerce.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for OrderController with MockMvc
 * Tests de controlador para OrderController con MockMvc
 */
@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test-nodb")
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderMapper orderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Order order;
    private OrderDTO orderDTO;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        OrderItem orderItem = OrderItem.builder()
                .productId(1L)
                .productName("Laptop HP")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .subtotal(new BigDecimal("1999.98"))
                .build();

        order = Order.builder()
                .id("order-001")
                .userId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1999.98"))
                .shippingAddress("Av. Principal 123, Lima, Peru")
                .paymentMethod("CREDIT_CARD")
                .notes("Test order")
                .items(List.of(orderItem))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .productId(1L)
                .productName("Laptop HP")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .build();

        orderDTO = OrderDTO.builder()
                .userId(1L)
                .shippingAddress("Av. Principal 123, Lima, Peru")
                .paymentMethod("CREDIT_CARD")
                .notes("Test order")
                .items(List.of(orderItemDTO))
                .build();

        OrderItemResponse itemResponse = OrderItemResponse.builder()
                .productId(1L)
                .productName("Laptop HP")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .subtotal(new BigDecimal("1999.98"))
                .build();

        orderResponse = OrderResponse.builder()
                .id("order-001")
                .userId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("1999.98"))
                .shippingAddress("Av. Principal 123, Lima, Peru")
                .paymentMethod("CREDIT_CARD")
                .notes("Test order")
                .items(List.of(itemResponse))
                .itemCount(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --- POST /api/orders ---

    @Test
    @DisplayName("POST /api/orders - request valido - debe retornar 201")
    void createOrder_ValidRequest_Returns201() throws Exception {
        // Given
        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido creado exitosamente"))
                .andExpect(jsonPath("$.data.id").value("order-001"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        verify(orderService).createOrder(any(OrderDTO.class));
    }

    @Test
    @DisplayName("POST /api/orders - sin userId - debe retornar 400")
    void createOrder_MissingUserId_Returns400() throws Exception {
        // Given
        orderDTO.setUserId(null);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("POST /api/orders - sin shippingAddress - debe retornar 400")
    void createOrder_MissingShippingAddress_Returns400() throws Exception {
        // Given
        orderDTO.setShippingAddress(null);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("POST /api/orders - pedido vacio - debe retornar 400")
    void createOrder_EmptyOrder_Returns400() throws Exception {
        // Given
        when(orderService.createOrder(any(OrderDTO.class)))
                .thenThrow(new EmptyOrderException());

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    // --- GET /api/orders/{id} ---

    @Test
    @DisplayName("GET /api/orders/{id} - ID existente - debe retornar 200")
    void getOrderById_ExistingId_Returns200() throws Exception {
        // Given
        when(orderService.getOrderById("order-001")).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(get("/api/orders/order-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("order-001"))
                .andExpect(jsonPath("$.data.userId").value(1));

        verify(orderService).getOrderById("order-001");
    }

    @Test
    @DisplayName("GET /api/orders/{id} - ID no existente - debe retornar 404")
    void getOrderById_NonExistingId_Returns404() throws Exception {
        // Given
        when(orderService.getOrderById("nonexistent"))
                .thenThrow(new OrderNotFoundException("nonexistent"));

        // When & Then
        mockMvc.perform(get("/api/orders/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    // --- GET /api/orders ---

    @Test
    @DisplayName("GET /api/orders - debe retornar lista de pedidos")
    void getAllOrders_ReturnsOrderList() throws Exception {
        // Given
        List<Order> orders = Arrays.asList(order, order);
        List<OrderResponse> responses = Arrays.asList(orderResponse, orderResponse);

        when(orderService.getAllOrders()).thenReturn(orders);
        when(orderMapper.toResponseList(orders)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(orderService).getAllOrders();
    }

    // --- GET /api/orders/user/{userId} ---

    @Test
    @DisplayName("GET /api/orders/user/{userId} - debe retornar pedidos del usuario")
    void getOrdersByUserId_ReturnsUserOrders() throws Exception {
        // Given
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        // When & Then
        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)));

        verify(orderService).getOrdersByUserId(1L);
    }

    // --- GET /api/orders/status/{status} ---

    @Test
    @DisplayName("GET /api/orders/status/{status} - debe retornar pedidos por estado")
    void getOrdersByStatus_ReturnsOrdersByStatus() throws Exception {
        // Given
        when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        // When & Then
        mockMvc.perform(get("/api/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(orderService).getOrdersByStatus(OrderStatus.PENDING);
    }

    // --- GET /api/orders/user/{userId}/status/{status} ---

    @Test
    @DisplayName("GET /api/orders/user/{userId}/status/{status} - debe filtrar por usuario y estado")
    void getOrdersByUserIdAndStatus_ReturnsFilteredOrders() throws Exception {
        // Given
        when(orderService.getOrdersByUserIdAndStatus(1L, OrderStatus.PENDING))
                .thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        // When & Then
        mockMvc.perform(get("/api/orders/user/1/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(orderService).getOrdersByUserIdAndStatus(1L, OrderStatus.PENDING);
    }

    // --- GET /api/orders/user/{userId}/recent ---

    @Test
    @DisplayName("GET /api/orders/user/{userId}/recent - debe retornar pedidos recientes")
    void getRecentOrdersByUserId_ReturnsRecentOrders() throws Exception {
        // Given
        when(orderService.getRecentOrdersByUserId(1L)).thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        // When & Then
        mockMvc.perform(get("/api/orders/user/1/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(orderService).getRecentOrdersByUserId(1L);
    }

    // --- PUT /api/orders/{id} ---

    @Test
    @DisplayName("PUT /api/orders/{id} - request valido - debe retornar 200")
    void updateOrder_ValidRequest_Returns200() throws Exception {
        // Given
        when(orderService.updateOrder(eq("order-001"), any(OrderDTO.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(put("/api/orders/order-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido actualizado exitosamente"))
                .andExpect(jsonPath("$.data.id").value("order-001"));

        verify(orderService).updateOrder(eq("order-001"), any(OrderDTO.class));
    }

    @Test
    @DisplayName("PUT /api/orders/{id} - ID no existente - debe retornar 404")
    void updateOrder_NonExistingId_Returns404() throws Exception {
        // Given
        when(orderService.updateOrder(eq("nonexistent"), any(OrderDTO.class)))
                .thenThrow(new OrderNotFoundException("nonexistent"));

        // When & Then
        mockMvc.perform(put("/api/orders/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/orders/{id} - estado invalido - debe retornar 400")
    void updateOrder_InvalidStatus_Returns400() throws Exception {
        // Given
        when(orderService.updateOrder(eq("order-001"), any(OrderDTO.class)))
                .thenThrow(new InvalidOrderStatusException(OrderStatus.PAID, OrderStatus.PENDING));

        // When & Then
        mockMvc.perform(put("/api/orders/order-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- PATCH /api/orders/{id}/status ---

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - transicion valida - debe retornar 200")
    void updateOrderStatus_ValidTransition_Returns200() throws Exception {
        // Given
        order.setStatus(OrderStatus.PAID);
        orderResponse.setStatus(OrderStatus.PAID);
        when(orderService.updateOrderStatus("order-001", OrderStatus.PAID)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        Map<String, String> statusRequest = Map.of("status", "PAID");

        // When & Then
        mockMvc.perform(patch("/api/orders/order-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estado del pedido actualizado exitosamente"));

        verify(orderService).updateOrderStatus("order-001", OrderStatus.PAID);
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - transicion invalida - debe retornar 400")
    void updateOrderStatus_InvalidTransition_Returns400() throws Exception {
        // Given
        when(orderService.updateOrderStatus("order-001", OrderStatus.SHIPPED))
                .thenThrow(new InvalidOrderStatusException(OrderStatus.PENDING, OrderStatus.SHIPPED));

        Map<String, String> statusRequest = Map.of("status", "SHIPPED");

        // When & Then
        mockMvc.perform(patch("/api/orders/order-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- PATCH /api/orders/{id}/cancel ---

    @Test
    @DisplayName("PATCH /api/orders/{id}/cancel - pedido cancelable - debe retornar 200")
    void cancelOrder_CancellableOrder_Returns200() throws Exception {
        // Given
        order.setStatus(OrderStatus.CANCELLED);
        orderResponse.setStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrder("order-001")).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(patch("/api/orders/order-001/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido cancelado exitosamente"));

        verify(orderService).cancelOrder("order-001");
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/cancel - pedido no cancelable - debe retornar 400")
    void cancelOrder_NonCancellableOrder_Returns400() throws Exception {
        // Given
        when(orderService.cancelOrder("order-001"))
                .thenThrow(new InvalidOrderStatusException(OrderStatus.DELIVERED, OrderStatus.CANCELLED));

        // When & Then
        mockMvc.perform(patch("/api/orders/order-001/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // --- DELETE /api/orders/{id} ---

    @Test
    @DisplayName("DELETE /api/orders/{id} - ID existente - debe retornar 200")
    void deleteOrder_ExistingId_Returns200() throws Exception {
        // Given
        doNothing().when(orderService).deleteOrder("order-001");

        // When & Then
        mockMvc.perform(delete("/api/orders/order-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido eliminado exitosamente"));

        verify(orderService).deleteOrder("order-001");
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - ID no existente - debe retornar 404")
    void deleteOrder_NonExistingId_Returns404() throws Exception {
        // Given
        doThrow(new OrderNotFoundException("nonexistent"))
                .when(orderService).deleteOrder("nonexistent");

        // When & Then
        mockMvc.perform(delete("/api/orders/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // --- GET /api/orders/user/{userId}/count ---

    @Test
    @DisplayName("GET /api/orders/user/{userId}/count - debe retornar conteo")
    void countOrdersByUserId_ReturnsCount() throws Exception {
        // Given
        when(orderService.countOrdersByUserId(1L)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/orders/user/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));

        verify(orderService).countOrdersByUserId(1L);
    }

    // --- GET /api/orders/status/{status}/count ---

    @Test
    @DisplayName("GET /api/orders/status/{status}/count - debe retornar conteo")
    void countOrdersByStatus_ReturnsCount() throws Exception {
        // Given
        when(orderService.countOrdersByStatus(OrderStatus.PENDING)).thenReturn(3L);

        // When & Then
        mockMvc.perform(get("/api/orders/status/PENDING/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(3));

        verify(orderService).countOrdersByStatus(OrderStatus.PENDING);
    }

    // --- GET /api/orders/user/{userId}/exists ---

    @Test
    @DisplayName("GET /api/orders/user/{userId}/exists - usuario con pedidos - debe retornar true")
    void userHasOrders_UserWithOrders_ReturnsTrue() throws Exception {
        // Given
        when(orderService.userHasOrders(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/orders/user/1/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("El usuario tiene pedidos"))
                .andExpect(jsonPath("$.data").value(true));

        verify(orderService).userHasOrders(1L);
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId}/exists - usuario sin pedidos - debe retornar false")
    void userHasOrders_UserWithoutOrders_ReturnsFalse() throws Exception {
        // Given
        when(orderService.userHasOrders(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/orders/user/999/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("El usuario no tiene pedidos"))
                .andExpect(jsonPath("$.data").value(false));
    }
}
