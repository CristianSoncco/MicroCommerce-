package com.microcommerce.order.controller;

import com.microcommerce.order.dto.OrderDTO;
import com.microcommerce.order.dto.response.ApiResponse;
import com.microcommerce.order.dto.response.OrderResponse;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.mapper.OrderMapper;
import com.microcommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Order operations
 * Controlador REST para operaciones de Pedidos
 *
 * Provides endpoints for creating, reading, updating, and deleting orders.
 * Provee endpoints para crear, leer, actualizar y eliminar pedidos.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "API de gestion de pedidos | Order management API")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    /**
     * Create a new order
     * Crear un nuevo pedido
     */
    @PostMapping
    @Operation(
        summary = "Crear pedido | Create order",
        description = "Crea un nuevo pedido en el sistema | Creates a new order in the system"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Pedido creado exitosamente | Order created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de entrada invalidos | Invalid input data"
        )
    })
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderDTO orderDTO) {

        log.info("Solicitud para crear pedido del usuario: {}", orderDTO.getUserId());

        Order order = orderService.createOrder(orderDTO);
        OrderResponse response = orderMapper.toResponse(order);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pedido creado exitosamente", response));
    }

    /**
     * Get order by ID
     * Obtener pedido por ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener pedido por ID | Get order by ID",
        description = "Obtiene un pedido por su ID | Gets an order by its ID"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido encontrado | Order found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado | Order not found"
        )
    })
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @Parameter(description = "ID del pedido | Order ID")
            @PathVariable String id) {

        log.info("Solicitud para obtener pedido con ID: {}", id);

        Order order = orderService.getOrderById(id);
        OrderResponse response = orderMapper.toResponse(order);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all orders
     * Obtener todos los pedidos
     */
    @GetMapping
    @Operation(
        summary = "Listar todos los pedidos | List all orders",
        description = "Obtiene la lista de todos los pedidos | Gets the list of all orders"
    )
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {

        log.info("Solicitud para obtener todos los pedidos");

        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> responses = orderMapper.toResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get orders by user ID
     * Obtener pedidos por ID de usuario
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Obtener pedidos por usuario | Get orders by user",
        description = "Obtiene todos los pedidos de un usuario | Gets all orders for a user"
    )
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long userId) {

        log.info("Solicitud para obtener pedidos del usuario: {}", userId);

        List<Order> orders = orderService.getOrdersByUserId(userId);
        List<OrderResponse> responses = orderMapper.toResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get orders by status
     * Obtener pedidos por estado
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Obtener pedidos por estado | Get orders by status",
        description = "Obtiene todos los pedidos con un estado determinado | Gets all orders with a given status"
    )
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @Parameter(description = "Estado del pedido | Order status")
            @PathVariable OrderStatus status) {

        log.info("Solicitud para obtener pedidos con estado: {}", status);

        List<Order> orders = orderService.getOrdersByStatus(status);
        List<OrderResponse> responses = orderMapper.toResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get orders by user ID and status
     * Obtener pedidos por ID de usuario y estado
     */
    @GetMapping("/user/{userId}/status/{status}")
    @Operation(
        summary = "Obtener pedidos por usuario y estado | Get orders by user and status",
        description = "Obtiene pedidos de un usuario filtrados por estado | Gets orders for a user filtered by status"
    )
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserIdAndStatus(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long userId,
            @Parameter(description = "Estado del pedido | Order status")
            @PathVariable OrderStatus status) {

        log.info("Solicitud para obtener pedidos del usuario {} con estado {}", userId, status);

        List<Order> orders = orderService.getOrdersByUserIdAndStatus(userId, status);
        List<OrderResponse> responses = orderMapper.toResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get recent orders by user ID
     * Obtener pedidos recientes por ID de usuario
     */
    @GetMapping("/user/{userId}/recent")
    @Operation(
        summary = "Obtener pedidos recientes del usuario | Get user recent orders",
        description = "Obtiene los pedidos mas recientes de un usuario | Gets the most recent orders for a user"
    )
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRecentOrdersByUserId(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long userId) {

        log.info("Solicitud para obtener pedidos recientes del usuario: {}", userId);

        List<Order> orders = orderService.getRecentOrdersByUserId(userId);
        List<OrderResponse> responses = orderMapper.toResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get orders by date range
     * Obtener pedidos por rango de fechas
     */
    @GetMapping("/date-range")
    @Operation(
        summary = "Obtener pedidos por rango de fechas | Get orders by date range",
        description = "Obtiene pedidos creados dentro de un rango de fechas | Gets orders created within a date range"
    )
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByDateRange(
            @Parameter(description = "Fecha inicio (ISO) | Start date (ISO)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha fin (ISO) | End date (ISO)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Solicitud para obtener pedidos entre {} y {}", startDate, endDate);

        List<Order> orders = orderService.getOrdersByDateRange(startDate, endDate);
        List<OrderResponse> responses = orderMapper.toResponseList(orders);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Update order
     * Actualizar pedido
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar pedido | Update order",
        description = "Actualiza un pedido existente (solo en estado PENDING) | Updates an existing order (only in PENDING status)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido actualizado | Order updated"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Estado invalido para actualizacion | Invalid status for update"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado | Order not found"
        )
    })
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @Parameter(description = "ID del pedido | Order ID")
            @PathVariable String id,
            @Valid @RequestBody OrderDTO orderDTO) {

        log.info("Solicitud para actualizar pedido con ID: {}", id);

        Order order = orderService.updateOrder(id, orderDTO);
        OrderResponse response = orderMapper.toResponse(order);

        return ResponseEntity.ok(ApiResponse.success("Pedido actualizado exitosamente", response));
    }

    /**
     * Update order status
     * Actualizar estado del pedido
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Actualizar estado del pedido | Update order status",
        description = "Actualiza el estado de un pedido con validacion de transiciones | Updates order status with transition validation"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estado actualizado | Status updated"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Transicion de estado invalida | Invalid status transition"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado | Order not found"
        )
    })
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "ID del pedido | Order ID")
            @PathVariable String id,
            @RequestBody Map<String, String> statusRequest) {

        log.info("Solicitud para actualizar estado del pedido: {}", id);

        OrderStatus newStatus = OrderStatus.valueOf(statusRequest.get("status"));
        Order order = orderService.updateOrderStatus(id, newStatus);
        OrderResponse response = orderMapper.toResponse(order);

        return ResponseEntity.ok(ApiResponse.success("Estado del pedido actualizado exitosamente", response));
    }

    /**
     * Cancel order
     * Cancelar pedido
     */
    @PatchMapping("/{id}/cancel")
    @Operation(
        summary = "Cancelar pedido | Cancel order",
        description = "Cancela un pedido si es posible segun su estado actual | Cancels an order if possible based on its current status"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido cancelado | Order cancelled"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "No se puede cancelar el pedido | Cannot cancel the order"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado | Order not found"
        )
    })
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Parameter(description = "ID del pedido | Order ID")
            @PathVariable String id) {

        log.info("Solicitud para cancelar pedido: {}", id);

        Order order = orderService.cancelOrder(id);
        OrderResponse response = orderMapper.toResponse(order);

        return ResponseEntity.ok(ApiResponse.success("Pedido cancelado exitosamente", response));
    }

    /**
     * Delete order
     * Eliminar pedido
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar pedido | Delete order",
        description = "Elimina un pedido del sistema | Deletes an order from the system"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido eliminado | Order deleted"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado | Order not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @Parameter(description = "ID del pedido | Order ID")
            @PathVariable String id) {

        log.info("Solicitud para eliminar pedido con ID: {}", id);

        orderService.deleteOrder(id);

        return ResponseEntity.ok(ApiResponse.success("Pedido eliminado exitosamente", null));
    }

    /**
     * Count orders by user ID
     * Contar pedidos por ID de usuario
     */
    @GetMapping("/user/{userId}/count")
    @Operation(
        summary = "Contar pedidos del usuario | Count user orders",
        description = "Obtiene la cantidad de pedidos de un usuario | Gets the order count for a user"
    )
    public ResponseEntity<ApiResponse<Long>> countOrdersByUserId(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long userId) {

        log.info("Solicitud para contar pedidos del usuario: {}", userId);

        long count = orderService.countOrdersByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success("Total de pedidos del usuario", count));
    }

    /**
     * Count orders by status
     * Contar pedidos por estado
     */
    @GetMapping("/status/{status}/count")
    @Operation(
        summary = "Contar pedidos por estado | Count orders by status",
        description = "Obtiene la cantidad de pedidos con un estado determinado | Gets the order count for a given status"
    )
    public ResponseEntity<ApiResponse<Long>> countOrdersByStatus(
            @Parameter(description = "Estado del pedido | Order status")
            @PathVariable OrderStatus status) {

        log.info("Solicitud para contar pedidos con estado: {}", status);

        long count = orderService.countOrdersByStatus(status);

        return ResponseEntity.ok(ApiResponse.success("Total de pedidos con estado " + status, count));
    }

    /**
     * Check if user has orders
     * Verificar si el usuario tiene pedidos
     */
    @GetMapping("/user/{userId}/exists")
    @Operation(
        summary = "Verificar si el usuario tiene pedidos | Check if user has orders",
        description = "Verifica si un usuario tiene al menos un pedido | Checks if a user has at least one order"
    )
    public ResponseEntity<ApiResponse<Boolean>> userHasOrders(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long userId) {

        log.info("Solicitud para verificar si el usuario {} tiene pedidos", userId);

        boolean hasOrders = orderService.userHasOrders(userId);
        String message = hasOrders
                ? "El usuario tiene pedidos"
                : "El usuario no tiene pedidos";

        return ResponseEntity.ok(ApiResponse.success(message, hasOrders));
    }
}
