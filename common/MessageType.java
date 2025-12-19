package common;

public enum MessageType {
    LOGIN, // Sent by client to identify itself
    START_MONITOR, // Sent by server to tell client to watch a folder
    FILE_CHANGE, // Sent by client when a file event occurs
    ERROR // For error reporting
}
