package com.microcommerce.payment.controller;

import com.microcommerce.payment.dto.request.PaymentRequest;
import com.microcommerce.payment.dto.request.RefundRequest;
import com.microcommerce.payment.dto.response.ApiResponse;
import com.microcommerce.payment.dto.response.PaymentResponse;
import com.microcommerce.payment.entity.PaymentStatus;
import com.microcommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Payment operations
 * Controlador REST para operaciones de Pagos
 *
 * Provides endpoints for processing payments, refunds, and querying payment information.
 * Provee endpoints para procesar pagos, reembolsos y consultar informacion de pagos.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "API de gestion de pagos | Payment management API")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Process a new payment
     * Procesar un nuevo pago
     */
    @PostMapping
    @Operation(
        summary = "Procesar pago | Process payment",
        description = "Procesa un nuevo pago a traves de Stripe | Processes a new payment through Stripe"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Pago procesado exitosamente | Payment processed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de entrada invalidos | Invalid input data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Pago ya procesado para esta orden | Payment already processed for this order"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "503",
            description = "Pasarela de pago no disponible | Payment gateway unavailable"
        )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {

        log.info("Solicitud para procesar pago de orden: {}, usuario: {}", request.getOrderId(), request.getUserId());

        PaymentResponse response = paymentService.processPayment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pago procesado exitosamente", response));
    }

    /**
     * Get all payments
     * Obtener todos los pagos
     */
    @GetMapping
    @Operation(
        summary = "Listar pagos | List payments",
        description = "Obtiene todos los pagos registrados | Gets all registered payments"
    )
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {

        log.info("Solicitud para listar todos los pagos");

        List<PaymentResponse> responses = paymentService.getAllPayments();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get payment by ID
     * Obtener pago por ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener pago por ID | Get payment by ID",
        description = "Obtiene un pago por su ID | Gets a payment by its ID"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pago encontrado | Payment found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado | Payment not found"
        )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @Parameter(description = "ID del pago | Payment ID")
            @PathVariable Long id) {

        log.info("Solicitud para obtener pago con ID: {}", id);

        PaymentResponse response = paymentService.getPaymentById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get payment by Stripe PaymentIntent ID
     * Obtener pago por ID de PaymentIntent de Stripe
     */
    @GetMapping("/stripe/{stripePaymentIntentId}")
    @Operation(
        summary = "Obtener pago por Stripe ID | Get payment by Stripe ID",
        description = "Obtiene un pago por su ID de PaymentIntent de Stripe | Gets a payment by its Stripe PaymentIntent ID"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pago encontrado | Payment found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado | Payment not found"
        )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByStripeIntentId(
            @Parameter(description = "ID de PaymentIntent de Stripe | Stripe PaymentIntent ID")
            @PathVariable String stripePaymentIntentId) {

        log.info("Solicitud para obtener pago con Stripe PaymentIntent ID: {}", stripePaymentIntentId);

        PaymentResponse response = paymentService.getPaymentByStripeIntentId(stripePaymentIntentId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get payments by order ID
     * Obtener pagos por ID de orden
     */
    @GetMapping("/order/{orderId}")
    @Operation(
        summary = "Obtener pagos por orden | Get payments by order",
        description = "Obtiene todos los pagos asociados a una orden | Gets all payments associated with an order"
    )
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByOrderId(
            @Parameter(description = "ID de la orden | Order ID")
            @PathVariable String orderId) {

        log.info("Solicitud para obtener pagos de la orden: {}", orderId);

        List<PaymentResponse> responses = paymentService.getPaymentsByOrderId(orderId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get payments by user ID
     * Obtener pagos por ID de usuario
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Obtener pagos por usuario | Get payments by user",
        description = "Obtiene todos los pagos de un usuario | Gets all payments for a user"
    )
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUserId(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long userId) {

        log.info("Solicitud para obtener pagos del usuario: {}", userId);

        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get payments by status
     * Obtener pagos por estado
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Obtener pagos por estado | Get payments by status",
        description = "Obtiene todos los pagos con un estado determinado | Gets all payments with a given status"
    )
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByStatus(
            @Parameter(description = "Estado del pago | Payment status")
            @PathVariable PaymentStatus status) {

        log.info("Solicitud para obtener pagos con estado: {}", status);

        List<PaymentResponse> responses = paymentService.getPaymentsByStatus(status);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get payments by user ID and status
     * Obtener pagos por ID de usuario y estado
     */
    @GetMapping("/user/{userId}/status/{status}")
    @Operation(
        summary = "Obtener pagos por usuario y estado | Get payments by user and status",
        description = "Obtiene pagos de un usuario filtrados por estado | Gets payments for a user filtered by status"
    )
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUserIdAndStatus(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long userId,
            @Parameter(description = "Estado del pago | Payment status")
            @PathVariable PaymentStatus status) {

        log.info("Solicitud para obtener pagos del usuario {} con estado {}", userId, status);

        List<PaymentResponse> responses = paymentService.getPaymentsByUserIdAndStatus(userId, status);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Refund a payment
     * Reembolsar un pago
     */
    @PostMapping("/{id}/refund")
    @Operation(
        summary = "Reembolsar pago | Refund payment",
        description = "Procesa un reembolso para un pago completado | Processes a refund for a completed payment"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reembolso procesado exitosamente | Refund processed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Estado de pago invalido para reembolso | Invalid payment state for refund"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado | Payment not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "503",
            description = "Pasarela de pago no disponible | Payment gateway unavailable"
        )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @Parameter(description = "ID del pago | Payment ID")
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {

        log.info("Solicitud para reembolsar pago con ID: {}", id);

        PaymentResponse response = paymentService.refundPayment(id, request);

        return ResponseEntity.ok(ApiResponse.success("Reembolso procesado exitosamente", response));
    }

    /**
     * Cancel a pending payment
     * Cancelar un pago pendiente
     */
    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Cancelar pago | Cancel payment",
        description = "Cancela un pago pendiente | Cancels a pending payment"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pago cancelado exitosamente | Payment cancelled successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Estado de pago invalido para cancelacion | Invalid payment state for cancellation"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado | Payment not found"
        )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @Parameter(description = "ID del pago | Payment ID")
            @PathVariable Long id) {

        log.info("Solicitud para cancelar pago con ID: {}", id);

        PaymentResponse response = paymentService.cancelPayment(id);

        return ResponseEntity.ok(ApiResponse.success("Pago cancelado exitosamente", response));
    }
}
