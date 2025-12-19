package server;

import common.Message;
import common.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerApp server;
    private String clientName;
    private boolean isRunning;

    public ClientHandler(Socket socket, ServerApp server) {
        this.socket = socket;
        this.server = server;
        this.isRunning = true;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (isRunning) {
                try {
                    Message msg = (Message) in.readObject();
                    handleMessage(msg);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // Client disconnected
        } finally {
            close();
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
            case LOGIN:
                this.clientName = msg.getSender();
                server.addClient(this);
                break;
            case FILE_CHANGE:
                server.log("Client [" + clientName + "]: " + msg.getPayload());
                break;
            case ERROR:
                server.log("Error from [" + clientName + "]: " + msg.getPayload());
                break;
            default:
                break;
        }
    }

    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientName() {
        return clientName;
    }

    private void close() {
        isRunning = false;
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
        server.removeClient(this);
    }
}
