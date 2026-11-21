
package com.project.cravely.services.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, Object event) {

        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish Kafka event", exception);
                    } else {
                        log.info(
                                "Kafka event published. Topic={}, Partition={}",
                                topic,
                                result.getRecordMetadata().partition()
                        );
                    }
                });
    }
}
