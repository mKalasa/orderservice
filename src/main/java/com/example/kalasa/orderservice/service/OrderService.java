package com.example.kalasa.orderservice.service;

import com.example.kalasa.orderservice.cliente.InventoryServiceClient;
import com.example.kalasa.orderservice.entity.Order;
import com.example.kalasa.bookingservice.event.BookingEvent;
import com.example.kalasa.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    @KafkaListener(topics = "booking", groupId = "order-service")
    public void orderEvent(BookingEvent bookingEvent) {
        log.info("Received booking event: {}", bookingEvent);
        // Create order object for DB
        Order order = createOrder(bookingEvent);
        orderRepository.saveAndFlush(order);
        // Update inventory service
        inventoryServiceClient.updateInventory(order.getEventId(), order.getTicketCount());
        log.info("Order created and inventory updated: {}", order);
    }

    private Order createOrder(BookingEvent bookingEvent) {
        return Order.builder()
                .customerId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }
}
