import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class NetworkDesktopManagerServer extends JFrame implements ActionListener {

    private static final int PORT = 1234;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private JTextArea chatArea;
    private JTextField chatField;
    private JButton sendButton;
    private JButton fileButton;
    private JButton desktopButton;

    public NetworkDesktopManagerServer() throws IOException {
        super("Network Desktop Manager Server");
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
        fileButton = new JButton("Receive File");
        fileButton.addActionListener(this);
        add(fileButton, BorderLayout.WEST);

        desktopButton = new JButton("Receive Desktop");
        desktopButton.addActionListener(this);
        add(desktopButton, BorderLayout.NORTH);

        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        serverSocket = new ServerSocket(PORT);
        chatArea.append("Waiting for client to connect...\n");

        
        clientSocket = serverSocket.accept();
        chatArea.append("Client connected");
        input = new DataInputStream(clientSocket.getInputStream());
        output = new DataOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String message = chatField.getText();
            try {
                output.writeUTF(message);
                chatArea.append("Server: " + message + "\n");
            } catch (IOException ex) {
                chatArea.append("Error sending message: " + ex.getMessage() + "\n");
            }
            chatField.setText("");
        } else if (e.getSource() == fileButton) {
            receiveFiles();
        } else if (e.getSource() == desktopButton) {
            receiveDesktop();
        }
    }

    private void receiveFiles() {
        try {
            int numFiles = input.readInt(); 
            for (int i = 0; i < numFiles; i++) {
                long fileSize = input.readLong();
                byte[] buffer = new byte[4096];
                FileOutputStream fileOutputStream = new FileOutputStream("received_file_" + i + ".txt");
                int bytesRead;
                while (fileSize > 0 && (bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    fileSize -= bytesRead;
                }
                fileOutputStream.close();
                chatArea.append("File " + i + " received successfully!\n");
            }
        } catch (IOException ex) {
            chatArea.append("Error receiving files: " + ex.getMessage() + "\n");
        }
    }

    

        private void receiveDesktop() {
            try {
                int numPackets = input.readInt(); 
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
                for (int i = 0; i < numPackets; i++) {
                    byte[] packet = new byte[1024]; 
                    int bytesRead = input.read(packet); 
                    baos.write(packet, 0, bytesRead); 
                }
        
                byte[] desktopBytes = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(desktopBytes);
                BufferedImage desktopImage = ImageIO.read(bais);
        
                
                JFrame imageFrame = new JFrame("Received Desktop Image");
                JLabel imageLabel = new JLabel(new ImageIcon(desktopImage));
                imageFrame.getContentPane().add(imageLabel);
                imageFrame.pack();
                imageFrame.setVisible(true);
        
                chatArea.append("Desktop image received successfully!\n");
            } catch (IOException ex) {
                chatArea.append("Error receiving desktop: " + ex.getMessage() + "\n");
            }
        }
        
    

    public static void main(String[] args) {
        try {
            new NetworkDesktopManagerServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


   
