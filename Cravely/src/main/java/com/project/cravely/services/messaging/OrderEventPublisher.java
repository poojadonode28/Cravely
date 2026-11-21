
package com.project.cravely.services.messaging;

import com.project.cravely.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaProducerService kafkaProducerService;

    @Value("${cravely.kafka.topics.order-created:order-created}")
    private String orderCreatedTopic;

    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaProducerService.publish(
                orderCreatedTopic,
                event.orderId().toString(),
                event
        );
    }
}
