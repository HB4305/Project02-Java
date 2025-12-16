package client;

import common.Message;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.net.InetAddress;

public class ClientApp extends JFrame {
    private JTextArea statusArea;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientName;
    private Thread watcherThread; // Luồng giám sát file

    public ClientApp() {
        setTitle("Client Monitor");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        add(new JScrollPane(statusArea), BorderLayout.CENTER);

        // Tự động kết nối khi chạy
        new Thread(this::connectToServer).start();
    }

    private void connectToServer() {
        try {
            // Lấy tên máy tính làm tên Client
            clientName = InetAddress.getLocalHost().getHostName();
            log("Đang kết nối tới Server (localhost:9999)...");

            // LƯU Ý: Thay "localhost" bằng IP của máy Server nếu chạy 2 máy khác nhau
            socket = new Socket("localhost", 9999);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Gửi tin nhắn đăng nhập
            out.writeObject(new Message(Message.Type.LOGIN, clientName, "Hello"));
            log("Đã kết nối! Đang chờ lệnh từ Server...");

            // Vòng lặp lắng nghe lệnh từ Server
            while (true) {
                Message msg = (Message) in.readObject();
                if (msg.getType() == Message.Type.START_MONITOR) {
                    String path = msg.getContent();
                    log("Server yêu cầu giám sát: " + path);
                    restartWatcher(path);
                }
            }
        } catch (Exception e) {
            log("Lỗi kết nối: " + e.getMessage());
        }
    }

    // Hàm khởi động lại WatchService cho đường dẫn mới
    private void restartWatcher(String pathStr) {
        // Nếu đang chạy cái cũ thì tắt đi
        if (watcherThread != null && watcherThread.isAlive()) {
            watcherThread.interrupt();
        }

        watcherThread = new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(pathStr);

                // Đăng ký các sự kiện: Tạo, Xóa, Sửa
                path.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                log("Đang giám sát thư mục: " + pathStr);

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watchService.take(); // Chờ sự kiện xảy ra
                    } catch (InterruptedException x) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path fileName = (Path) event.context();

                        String notification = kind.name() + ": " + fileName;
                        log("Phát hiện: " + notification);

                        // Gửi báo cáo về Server
                        sendMessage(
                                new Message(Message.Type.FILE_CHANGE, clientName, notification + " tại " + pathStr));
                    }

                    boolean valid = key.reset();
                    if (!valid)
                        break;
                }
            } catch (IOException e) {
                sendMessage(new Message(Message.Type.ERROR, clientName, "Không thể đọc thư mục: " + e.getMessage()));
                log("Lỗi giám sát: " + e.getMessage());
            }
        });
        watcherThread.start();
    }

    private void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            log("Lỗi gửi tin: " + e.getMessage());
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> statusArea.append(msg + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientApp().setVisible(true));
    }
}