import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Server {
    private static ServerSocket forwardSocket;

    public static void main(String[] args) throws IOException {
        int port = 49153;
        int forwardPort = 49154;
        ServerSocket serverSocket = new ServerSocket(port);
        forwardSocket = new ServerSocket(forwardPort);
        System.out.println("Server started");
        System.out.println("Forwarding server started");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            Socket forwardClient = forwardSocket.accept();
            System.out.println("Client connected: " + clientSocket);
            new Thread(new Message(clientSocket, forwardClient)).start();
        }
    }

    private static class Message implements Runnable {
        private Socket clientSocket;
        private Socket forwardSocket;

        public Message(Socket clientSocket, Socket forwardSocket) {
            this.clientSocket = clientSocket;
            this.forwardSocket = forwardSocket;
        }

@Override
public void run() {
    try (
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
    ) {
        String message = (String) in.readObject();
        byte[] publicKeyBytes = (byte[]) in.readObject();
        byte[] signature = (byte[]) in.readObject();

        out.writeObject("Message received successfully");
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowSignatureEditor(message, signature, publicKeyBytes, out);
            }
        });

        clientSocket.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void createAndShowSignatureEditor(String message, byte[] signature, byte[] publicKeyBytes, ObjectOutputStream out) {
    JFrame frame = new JFrame("Edit Signature");
    JTextArea signatureTextArea = new JTextArea(10, 30);
    signatureTextArea.setText(bytesToHex(signature));
    JButton submitButton = new JButton("Submit");
    submitButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            byte[] editedSignature = hexToBytes(signatureTextArea.getText());
            
            forwardMessage(message, editedSignature, publicKeyBytes);

            frame.dispose();
        }
    });
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JScrollPane(signatureTextArea), BorderLayout.CENTER);
    panel.add(submitButton, BorderLayout.SOUTH);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(panel);
    frame.pack();
    frame.setVisible(true);
}
private String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
            hexString.append('0');
        }
        hexString.append(hex);
    }
    return hexString.toString();
}

private byte[] hexToBytes(String hexString) {
    int len = hexString.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                             + Character.digit(hexString.charAt(i+1), 16));
    }
    return data;
}


        private void forwardMessage(String message, byte[] signature,byte[] publicKey) {
            try (
                ObjectOutputStream out = new ObjectOutputStream(forwardSocket.getOutputStream());
            ) {
                out.writeObject(publicKey);
                out.writeObject(message);
                out.writeObject(signature);
                System.out.println("Message forwarded to ForwardMessage");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
