package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.model.Order;
import com.example.orderservice.service.IdempotencyService;
import com.example.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private OrderService orderService;

    private Counter orderCreateSuccess;
    private Counter orderCreateFailure;

    @PostConstruct
    public void initCounters() {
        this.orderCreateSuccess = meterRegistry.counter("orders.creation.success");
        this.orderCreateFailure = meterRegistry.counter("orders.creation.failure");
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createOrder(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                         @RequestBody CreateOrderRequest req,
                                         HttpServletRequest servletRequest) {
        String requestId = servletRequest.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();

        MDC.put("requestId", requestId);
        try {
            logger.info("event=createOrder start requestId={} idempotencyKey={} customerId={}", requestId, idempotencyKey, req == null ? null : req.getCustomerId());

            if (idempotencyKey != null && idempotencyService.exists(idempotencyKey)) {
                String prev = idempotencyService.getResponse(idempotencyKey);
                logger.info("event=createOrder idempotentHit requestId={} idempotencyKey={}", requestId, idempotencyKey);
                orderCreateSuccess.increment();
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(prev);
            }

            // Very simple validation
            if (req == null || req.getCustomerId() == null || req.getItems() == null || req.getItems().isEmpty()) {
                logger.warn("event=createOrder validationFailed requestId={} reason=missingFields", requestId);
                orderCreateFailure.increment();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid payload");
            }

            String orderId = orderService.createOrder(req.getCustomerId(), req.getItems(), idempotencyKey);

            logger.info("event=createOrder success requestId={} orderId={}", requestId, orderId);
            orderCreateSuccess.increment();
            return ResponseEntity.created(URI.create("/orders/" + orderId)).body(null);
        } catch (Exception ex) {
            logger.error("event=createOrder error requestId={} message={}", requestId, ex.getMessage(), ex);
            orderCreateFailure.increment();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal error");
        } finally {
            MDC.remove("requestId");
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getOrder(@PathVariable("id") String id, HttpServletRequest servletRequest) {
        String requestId = servletRequest.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.info("event=getOrder start requestId={} orderId={}", requestId, id);
            // load from repository
            com.example.orderservice.persistence.OrderEntity e = null; // TODO: call repository/service
            if (e==null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
            // map entity to DTO
            return ResponseEntity.ok().body(e);
        } finally {
            MDC.remove("requestId");
        }
    }

    @PostMapping(value = "/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable String id,
                                     @RequestHeader(value = "Idempotency-Key", required = false) String idem,
                                     HttpServletRequest servletRequest) {
        String requestId = servletRequest.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.info("event=confirmOrder start requestId={} orderId={} idempotencyKey={}", requestId, id, idem);
            try { UUID.fromString(id); } catch (IllegalArgumentException ex) { logger.warn("event=confirmOrder invalidId requestId={} orderId={}", requestId, id); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid id"); }
            orderService.confirmOrder(id, idem);
            logger.info("event=confirmOrder success requestId={} orderId={}", requestId, id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            logger.error("event=confirmOrder error requestId={} orderId={} message={}", requestId, id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("event=confirmOrder error requestId={} orderId={} message={}", requestId, id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal error");
        } finally {
            MDC.remove("requestId");
        }
    }

    @PostMapping(value = "/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String id,
                                    @RequestHeader(value = "Idempotency-Key", required = false) String idem,
                                    HttpServletRequest servletRequest) {
        String requestId = servletRequest.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            logger.info("event=cancelOrder start requestId={} orderId={} idempotencyKey={}", requestId, id, idem);
            try { UUID.fromString(id); } catch (IllegalArgumentException ex) { logger.warn("event=cancelOrder invalidId requestId={} orderId={}", requestId, id); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid id"); }
            orderService.cancelOrder(id, idem);
            logger.info("event=cancelOrder success requestId={} orderId={}", requestId, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            logger.error("event=cancelOrder error requestId={} orderId={} message={}", requestId, id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("event=cancelOrder error requestId={} orderId={} message={}", requestId, id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal error");
        } finally {
            MDC.remove("requestId");
        }
    }
}
