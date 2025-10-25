package ru.lisa.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.lisa.event.EventType;
import ru.lisa.event.UserEvent;

import static ru.lisa.util.GsonUtil.GSON;

@Component
@RequiredArgsConstructor
public class UserEventProducerImpl implements UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("topic.name")
    private String topic;

    @Override
    public void send(EventType eventType, String email) {
        kafkaTemplate.send(topic, GSON.toJson((new UserEvent(EventType.DELETED, email))));
    }
}
