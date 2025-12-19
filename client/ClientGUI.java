package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI extends JFrame {
    private ClientApp clientApp;
    private JTextField hostField;
    private JTextField portField;
    private JTextField nameField;
    private JButton connectButton;
    private JTextArea logArea;

    public ClientGUI(ClientApp clientApp) {
        this.clientApp = clientApp;
        initUI();
    }

    private void initUI() {
        setTitle("Remote File Monitor Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel configPanel = new JPanel(new GridLayout(4, 2));
        configPanel.add(new JLabel("Server Host:"));
        hostField = new JTextField("localhost");
        configPanel.add(hostField);

        configPanel.add(new JLabel("Server Port:"));
        portField = new JTextField("12345");
        configPanel.add(portField);

        configPanel.add(new JLabel("Client Name:"));
        nameField = new JTextField("Client-" + System.currentTimeMillis() % 1000);
        configPanel.add(nameField);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Connect".equals(connectButton.getText())) {
                    String host = hostField.getText();
                    int port;
                    try {
                        port = Integer.parseInt(portField.getText());
                    } catch (NumberFormatException ex) {
                        appendLog("Invalid port.");
                        return;
                    }
                    String name = nameField.getText();
                    clientApp.connect(host, port, name);
                } else {
                    // Disconnect logic if needed, or just exit
                    System.exit(0);
                }
            }
        });
        configPanel.add(connectButton);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        add(configPanel, BorderLayout.NORTH);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void setConnected() {
        SwingUtilities.invokeLater(() -> {
            connectButton.setText("Disconnect");
            hostField.setEditable(false);
            portField.setEditable(false);
            nameField.setEditable(false);
        });
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}
