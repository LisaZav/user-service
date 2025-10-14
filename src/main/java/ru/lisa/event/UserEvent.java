package ru.lisa.event;

public class UserEvent {
    private EventType eventType;
    private String email;

    public UserEvent() {}

    public UserEvent(EventType eventType, String email) {
        this.eventType = eventType;
        this.email = email;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}