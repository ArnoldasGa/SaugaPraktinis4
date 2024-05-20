import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import javax.swing.*;

public class App {
    public static void main(String[] args) throws Exception {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                JFrame frame = new JFrame("Text input for RSA encoding");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JTextField textField = new JTextField(20);
                JButton sendButton = new JButton("Send");
                
                sendButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String text = textField.getText();
                        try {
                            KeyPair key = generateKeyPair();
                            PublicKey publicKey = key.getPublic();
                            byte[] encodedText = sign(textField.getText(), key.getPrivate());
                            String check = send(text, publicKey, encodedText);
                            JOptionPane.showMessageDialog(frame, "Server: " + check);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                        }
                    }
                });
                
                Container contentPane = frame.getContentPane();
                contentPane.setLayout(new FlowLayout());
                contentPane.add(textField);
                contentPane.add(sendButton);
                
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int centerX = (int) ((screenSize.getWidth() - frame.getWidth()) / 2);
                int centerY = (int) ((screenSize.getHeight() - frame.getHeight()) / 2);
                frame.setLocation(centerX, centerY);
                
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] sign(String text, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(text.getBytes());
        return signature.sign();
    }

    public static String send(String text, PublicKey publicKey, byte[] signature) throws Exception {
        String serverAddress = "127.0.0.1";
        int serverPort = 49153;
        Socket socket = new Socket(serverAddress, serverPort);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(text);
        byte[] publicKeyBytes = publicKey.getEncoded();
        out.writeObject(publicKeyBytes);
        out.writeObject(signature);

        String check = (String) in.readObject();
        return check;
    }
}
