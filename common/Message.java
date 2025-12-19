package common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private Object payload;
    private String sender;

    public Message(MessageType type, Object payload, String sender) {
        this.type = type;
        this.payload = payload;
        this.sender = sender;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public String getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return "Message{type=" + type + ", payload=" + payload + ", sender='" + sender + "'}";
    }
}
