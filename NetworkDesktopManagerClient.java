import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class NetworkDesktopManagerClient extends JFrame implements ActionListener {

    private static final int PORT = 1234;

    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendButton;
    private JButton fileButton;
    private JButton desktopButton;
    private boolean sharingDesktop; // Flag to control desktop sharing

    public NetworkDesktopManagerClient() throws IOException {
        super("Network Desktop Manager Client");
        setLayout(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatField = new JTextField();
        add(chatField, BorderLayout.SOUTH);
        /*
        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        add(sendButton, BorderLayout.EAST);
         */
        fileButton = new JButton("Send File");
        fileButton.addActionListener(this);
        add(fileButton, BorderLayout.WEST);
        desktopButton = new JButton("Share Desktop");
        desktopButton.addActionListener(this);
        add(desktopButton, BorderLayout.NORTH);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Connect to the server
        clientSocket = new Socket("192.168.60.212", PORT); // Replace with the server IP addres
        input = new DataInputStream(clientSocket.getInputStream());
        output = new DataOutputStream(clientSocket.getOutputStream());
        chatArea.append("Connected to server!\n");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String message = chatField.getText();
            try {
                output.writeUTF(message);
                chatArea.append("You: " + message + "\n");
            } catch (IOException ex) {
                chatArea.append("Error sending message: " + ex.getMessage() + "\n");
            }
            chatField.setText("");
        } else if (e.getSource() == fileButton) {
            sendFiles(); 
        } else if (e.getSource() == desktopButton) {
            sendDesktop();
        }
    }

    private void sendFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true); 
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            try {
                
                output.writeInt(selectedFiles.length);

                for (File file : selectedFiles) {
                    // Send file size
                    long fileSize = file.length();
                    output.writeLong(fileSize);

                    
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    fileInputStream.close();
                    chatArea.append("File '" + file.getName() + "' sent successfully!\n");
                }
            } catch (IOException ex) {
                chatArea.append("Error sending files: " + ex.getMessage() + "\n");
            }
        }
    }


private void sendDesktop() {
    try {
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;

        
        Robot robot = new Robot();

        
        BufferedImage desktopImage = robot.createScreenCapture(new Rectangle(0, 0, width, height));

        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(desktopImage, "png", baos);
        byte[] desktopBytes = baos.toByteArray();

        

        int packetSize = 1024; 
        int numPackets = (int) Math.ceil((double) desktopBytes.length / packetSize);
        output.writeInt(numPackets);

        for (int i = 0; i < numPackets; i++) {
            byte[] packet = new byte[packetSize];
            System.arraycopy(desktopBytes, i * packetSize, packet, 0, Math.min(packetSize, desktopBytes.length - i * packetSize));
            output.write(packet); 

        }

        chatArea.append("Desktop image sent successfully!\n");
    } catch (IOException | AWTException ex) {
        chatArea.append("Error sending desktop: " + ex.getMessage() + "\n");
    }
}

    public static void main(String[] args) {
        try {
            new NetworkDesktopManagerClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
