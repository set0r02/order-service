package com.innowise.orderservice.listener;

import com.innowise.orderservice.dto.event.PaymentEvent;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "order-service-group")
    public void handlePaymentEvent(PaymentEvent paymentEvent){

        log.info("Received payment event: {}", paymentEvent);

        orderService.handlePayment(paymentEvent.orderId(), paymentEvent.paymentStatus());

        log.info("Processed payment event for order {}", paymentEvent.orderId());
    }

}
