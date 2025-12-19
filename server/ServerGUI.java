package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame {
    private ServerApp serverApp;
    private JList<String> clientList;
    private DefaultListModel<String> listModel;
    private JTextArea logArea;
    private JButton monitorButton;
    private JTextField portField;
    private JButton startButton;

    public ServerGUI(ServerApp serverApp) {
        this.serverApp = serverApp;
        initUI();
    }

    private void initUI() {
        setTitle("Remote File Monitor Server");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel: Port Config
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Port:"));
        portField = new JTextField("12345", 5);
        topPanel.add(portField);
        startButton = new JButton("Start Server");
        startButton.addActionListener(e -> {
            try {
                int port = Integer.parseInt(portField.getText());
                serverApp.startServer(port);
                startButton.setEnabled(false);
                portField.setEditable(false);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Port Number");
            }
        });
        topPanel.add(startButton);

        // Client List
        listModel = new DefaultListModel<>();
        clientList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(clientList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Connected Clients"));
        listScrollPane.setPreferredSize(new Dimension(200, 0));

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Activity Log"));

        // Controls
        JPanel controlPanel = new JPanel();
        monitorButton = new JButton("Select Folder to Monitor");
        monitorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedClient = clientList.getSelectedValue();
                if (selectedClient != null) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int result = fileChooser.showOpenDialog(ServerGUI.this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        String path = fileChooser.getSelectedFile().getAbsolutePath();
                        serverApp.startMonitoring(selectedClient, path);
                    }
                } else {
                    JOptionPane.showMessageDialog(ServerGUI.this, "Please select a client first.");
                }
            }
        });
        controlPanel.add(monitorButton);

        add(topPanel, BorderLayout.NORTH);
        add(listScrollPane, BorderLayout.WEST);
        add(logScrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    public void addClient(String name) {
        SwingUtilities.invokeLater(() -> listModel.addElement(name));
    }

    public void removeClient(String name) {
        SwingUtilities.invokeLater(() -> listModel.removeElement(name));
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}
