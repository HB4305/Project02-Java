package server;

import common.Message;
import common.MessageType;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerApp {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private ServerGUI gui;
    private boolean isRunning;

    public ServerApp() {
        clients = new ArrayList<>();
        gui = new ServerGUI(this);
    }

    public void start() {
        gui.setVisible(true);
    }

    public void startServer(int port) {
        if (isRunning)
            return;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                isRunning = true;
                log("Server started on port " + port);

                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, this);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                log("Server Error: " + e.getMessage());
                isRunning = false;
            }
        }).start();
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        gui.addClient(client.getClientName());
        log("Client connected: " + client.getClientName());
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        if (client.getClientName() != null) {
            gui.removeClient(client.getClientName());
            log("Client disconnected: " + client.getClientName());
        }
    }

    public void log(String message) {
        gui.appendLog(message);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void startMonitoring(String clientName, String path) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equals(clientName)) {
                client.sendMessage(new Message(MessageType.START_MONITOR, path, "Server"));
                log("Requested monitoring for " + clientName + " on: " + path);
                return;
            }
        }
    }

    public static void main(String[] args) {
        new ServerApp().start();
    }
}
