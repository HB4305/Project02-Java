package client;

import common.Message;
import common.MessageType;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientApp {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientName;
    private FileWatcher currentWatcher;
    private ClientGUI gui;

    public ClientApp() {
        this(false);
    }

    protected ClientApp(boolean testMode) {
        if (!testMode) {
            gui = new ClientGUI(this);
            gui.setVisible(true);
        }
    }

    public void connect(String host, int port, String name) {
        this.clientName = name;
        new Thread(() -> {
            try {
                if (gui != null)
                    gui.appendLog("Connecting to " + host + ":" + port + "...");
                socket = new Socket(host, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                sendMessage(new Message(MessageType.LOGIN, null, clientName));
                if (gui != null) {
                    gui.appendLog("Connected as " + clientName);
                    gui.setConnected();
                }

                // Listen loop
                while (true) {
                    Message msg = (Message) in.readObject();
                    handleMessage(msg);
                }

            } catch (Exception e) {
                if (gui != null)
                    gui.appendLog("Connection Error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                close();
            }
        }).start();
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case START_MONITOR:
                String path = (String) msg.getPayload();
                if (gui != null)
                    gui.appendLog("Monitor request for: " + path);
                startWatcher(path);
                break;
            default:
                System.out.println("Unknown message: " + msg);
        }
    }

    private synchronized void startWatcher(String path) {
        if (currentWatcher != null) {
            currentWatcher.stop();
        }
        currentWatcher = new FileWatcher(path, this);
        new Thread(currentWatcher).start();
    }

    public synchronized void sendFileChange(String change) {
        sendMessage(new Message(MessageType.FILE_CHANGE, change, clientName));
        if (gui != null)
            gui.appendLog("Sent change: " + change);
    }

    public synchronized void sendError(String error) {
        sendMessage(new Message(MessageType.ERROR, error, clientName));
        if (gui != null)
            gui.appendLog("Error: " + error);
    }

    public void log(String message) {
        if (gui != null) {
            gui.appendLog(message);
        } else {
            System.out.println(message);
        }
    }

    private void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        if (currentWatcher != null)
            currentWatcher.stop();
        try {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientApp());
    }
}
