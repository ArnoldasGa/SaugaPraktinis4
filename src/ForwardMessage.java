import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import javax.swing.JOptionPane;

public class ForwardMessage {
    public static void main(String[] args) {
        try {
            String serverAddress = "127.0.0.1";
            int serverPort = 49154;
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server: " + socket);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            byte[] encodedPublicKey = (byte[]) in.readObject();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedPublicKey);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            System.out.println("Received public key from server: " + publicKey);

            String message = (String) in.readObject();
            byte[] signature = (byte[]) in.readObject();

            boolean signatureValid = verify(message, signature, publicKey);
            System.out.println("Signature verification result: " + (signatureValid ? "Valid" : "Invalid"));
            System.out.println("Received message from server: " + message);

            String confirmationMessage = "Message received from another person:\n" + message
                    + "\n\nSignature verification result: " + (signatureValid ? "Valid" : "Invalid");
            JOptionPane.showMessageDialog(null, confirmationMessage, "Message Received", JOptionPane.INFORMATION_MESSAGE);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean verify(String message, byte[] signature, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(message.getBytes());
        return sig.verify(signature);
    }
}
