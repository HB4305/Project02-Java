package common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    // Các loại tin nhắn
    public enum Type {
        LOGIN, // Client gửi tin nhắn đăng nhập
        START_MONITOR, // Server yêu cầu bắt đầu giám sát
        FILE_CHANGE, // Client báo cáo thay đổi file
        ERROR // Báo lỗi
    }

    private Type type;
    private String content; // Nội dung (ví dụ: đường dẫn, tên file)
    private String sender; // Tên máy gửi

    public Message(Type type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    // Getters
    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }
}