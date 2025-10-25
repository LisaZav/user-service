package ru.lisa.kafka;

import ru.lisa.event.EventType;

public interface UserEventProducer {
    void send(EventType eventType, String email);

}
