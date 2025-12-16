package server;

import common.Message;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerApp extends JFrame {
    private JTextArea logArea;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private JTextField pathField;
    private JButton monitorBtn;

    // Lưu trữ các kết nối client: Key là tên Client, Value là luồng xử lý tương ứng
    private Map<String, ClientHandler> clients = new HashMap<>();

    public ServerApp() {
        setTitle("Server Giám Sát Thư Mục");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Giao diện
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        JScrollPane listScroll = new JScrollPane(clientList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Danh sách Client Online"));
        listScroll.setPreferredSize(new Dimension(200, 0));

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Nhật ký thay đổi"));

        JPanel controlPanel = new JPanel(new BorderLayout());
        pathField = new JTextField("D:/TestFolder"); // Đường dẫn mặc định để test
        monitorBtn = new JButton("Bắt đầu giám sát");
        controlPanel.add(new JLabel("Đường dẫn trên Client: "), BorderLayout.WEST);
        controlPanel.add(pathField, BorderLayout.CENTER);
        controlPanel.add(monitorBtn, BorderLayout.EAST);

        add(listScroll, BorderLayout.WEST);
        add(logScroll, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Sự kiện nút Monitor
        monitorBtn.addActionListener(e -> startMonitoring());

        // Khởi chạy Server Socket
        new Thread(this::startServer).start();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            log("Server đang chạy tại port 9999...");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            log("Lỗi Server: " + e.getMessage());
        }
    }

    // Gửi lệnh giám sát tới Client được chọn
    private void startMonitoring() {
        String selectedClient = clientList.getSelectedValue();
        String path = pathField.getText().trim();

        if (selectedClient == null || path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Client và nhập đường dẫn!");
            return;
        }

        ClientHandler handler = clients.get(selectedClient);
        if (handler != null) {
            handler.sendMessage(new Message(Message.Type.START_MONITOR, "Server", path));
            log("Đã gửi yêu cầu giám sát '" + path + "' tới " + selectedClient);
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    // --- Inner Class để xử lý từng Client ---
    private class ClientHandler extends Thread {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Message msg = (Message) in.readObject();

                    switch (msg.getType()) {
                        case LOGIN:
                            this.clientName = msg.getSender();
                            clients.put(clientName, this);
                            SwingUtilities.invokeLater(() -> clientListModel.addElement(clientName));
                            log("Client kết nối: " + clientName);
                            break;

                        case FILE_CHANGE:
                            // Nhận thông báo thay đổi file
                            log("[" + msg.getSender() + "] THAY ĐỔI: " + msg.getContent());
                            break;

                        case ERROR:
                            log("[" + msg.getSender() + "] LỖI: " + msg.getContent());
                            break;
                    }
                }
            } catch (Exception e) {
                log("Client ngắt kết nối: " + clientName);
            } finally {
                // Dọn dẹp khi ngắt kết nối
                if (clientName != null) {
                    clients.remove(clientName);
                    SwingUtilities.invokeLater(() -> clientListModel.removeElement(clientName));
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        public void sendMessage(Message msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                log("Không thể gửi tin tới " + clientName);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerApp().setVisible(true));
    }
}