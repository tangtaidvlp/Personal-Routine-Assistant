package com.tom.payment.routinemanager.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/routine-manager")
public class RoutineManagerController {

    @PostMapping("/payment-requests")
    public ResponseEntity<RoutineManagerResponse> acceptPaymentRequest(@RequestBody PaymentRoutineManagerRequest request) {
        RoutineManagerResponse response = new RoutineManagerResponse(
                UUID.randomUUID().toString(),
                "ACCEPTED",
                "Payment request accepted",
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    public record PaymentRoutineManagerRequest(
            String customerId,
            BigDecimal amount,
            String currency,
            String description
    ) {
    }

    public record RoutineManagerResponse(
            String requestId,
            String status,
            String message,
            Instant acceptedAt
    ) {
    }

}
