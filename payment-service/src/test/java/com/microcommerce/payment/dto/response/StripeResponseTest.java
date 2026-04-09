package com.microcommerce.payment.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Stripe response DTOs
 * Tests unitarios para DTOs de respuesta de Stripe
 */
@DisplayName("Stripe Response DTO Tests")
class StripeResponseTest {

    // ==================== StripePaymentIntentResponse Tests ====================

    @Test
    @DisplayName("isSucceeded - status succeeded - debe retornar true")
    void stripePaymentIntent_StatusSucceeded_IsSucceededReturnsTrue() {
        // Given
        StripePaymentIntentResponse response = StripePaymentIntentResponse.builder()
                .id("pi_test_001")
                .status("succeeded")
                .build();

        // When & Then
        assertThat(response.isSucceeded()).isTrue();
        assertThat(response.requiresAction()).isFalse();
    }

    @Test
    @DisplayName("requiresAction - status requires_action - debe retornar true")
    void stripePaymentIntent_StatusRequiresAction_RequiresActionReturnsTrue() {
        // Given
        StripePaymentIntentResponse response = StripePaymentIntentResponse.builder()
                .id("pi_test_002")
                .status("requires_action")
                .build();

        // When & Then
        assertThat(response.requiresAction()).isTrue();
        assertThat(response.isSucceeded()).isFalse();
    }

    @Test
    @DisplayName("isSucceeded - status diferente - debe retornar false")
    void stripePaymentIntent_DifferentStatus_IsSucceededReturnsFalse() {
        // Given
        StripePaymentIntentResponse response = StripePaymentIntentResponse.builder()
                .id("pi_test_003")
                .status("canceled")
                .build();

        // When & Then
        assertThat(response.isSucceeded()).isFalse();
        assertThat(response.requiresAction()).isFalse();
    }

    @Test
    @DisplayName("isSucceeded - status null - debe retornar false")
    void stripePaymentIntent_NullStatus_IsSucceededReturnsFalse() {
        // Given
        StripePaymentIntentResponse response = StripePaymentIntentResponse.builder()
                .id("pi_test_004")
                .status(null)
                .build();

        // When & Then
        assertThat(response.isSucceeded()).isFalse();
        assertThat(response.requiresAction()).isFalse();
    }

    // ==================== StripeRefundResponse Tests ====================

    @Test
    @DisplayName("StripeRefundResponse isSucceeded - status succeeded - debe retornar true")
    void stripeRefund_StatusSucceeded_IsSucceededReturnsTrue() {
        // Given
        StripeRefundResponse response = StripeRefundResponse.builder()
                .id("re_test_001")
                .status("succeeded")
                .build();

        // When & Then
        assertThat(response.isSucceeded()).isTrue();
    }

    @Test
    @DisplayName("StripeRefundResponse isSucceeded - status pending - debe retornar false")
    void stripeRefund_StatusPending_IsSucceededReturnsFalse() {
        // Given
        StripeRefundResponse response = StripeRefundResponse.builder()
                .id("re_test_002")
                .status("pending")
                .build();

        // When & Then
        assertThat(response.isSucceeded()).isFalse();
    }

    @Test
    @DisplayName("StripeRefundResponse isSucceeded - status null - debe retornar false")
    void stripeRefund_NullStatus_IsSucceededReturnsFalse() {
        // Given
        StripeRefundResponse response = StripeRefundResponse.builder()
                .id("re_test_003")
                .status(null)
                .build();

        // When & Then
        assertThat(response.isSucceeded()).isFalse();
    }
}
