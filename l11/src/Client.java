import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {

    private static String currentUsername = " ";

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage : java Client <server_host> <server_port>");
            System.exit(1);
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try {
            DatagramSocket socket = new DatagramSocket();
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter your name: ");
            currentUsername = userInput.readLine();

            sendLoginNotification(socket, serverHost, serverPort);

            byte[] nameCommandData = ("@name" + currentUsername).getBytes();
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            DatagramPacket nameCommandPacket = new DatagramPacket(nameCommandData, nameCommandData.length,
                    serverAddress, serverPort);
            socket.send(nameCommandPacket);

            Thread receiveThread = new Thread(new MessageReceiver(socket));
            receiveThread.start();

            while (true) {
                System.out.println("Enter a message (or type '@quit' to exit) ");
                String message = userInput.readLine();

                if ("@quit".equals(message)) {
                    System.out.println("Exiting chat.");
                    sendQuitNotification(socket, serverHost, serverPort);
                    break;
                } else if (message.startsWith("@name")) {
                    currentUsername = message.substring(6).trim();
                    byte[] newNameCommandData = message.getBytes();
                    DatagramPacket newNameCommandPacket = new DatagramPacket(newNameCommandData, newNameCommandData.length,
                            serverAddress, serverPort);
                    socket.send(newNameCommandPacket);
                } else {
                    String messageWithUsername = currentUsername + ": " + message;
                    byte[] sendData = messageWithUsername.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                            serverAddress, serverPort);
                    socket.send(sendPacket);
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void sendLoginNotification(DatagramSocket socket, String serverHost, int serverPort) throws IOException {
        String loginMessage = currentUsername + " has logged in.";
        byte[] loginData = loginMessage.getBytes();
        DatagramPacket loginPacket = new DatagramPacket(loginData, loginData.length,
                InetAddress.getByName(serverHost), serverPort);
        socket.send(loginPacket);
    }

    private static void sendQuitNotification(DatagramSocket socket, String serverHost, int serverPort) throws IOException {
        String quitMessage = currentUsername + " has logged out.";
        byte[] loginData = quitMessage.getBytes();
        DatagramPacket loginPacket = new DatagramPacket(loginData, loginData.length,
                InetAddress.getByName(serverHost), serverPort);
        socket.send(loginPacket);
    }

    static class MessageReceiver implements Runnable {
        private DatagramSocket socket;

        public MessageReceiver(DatagramSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);

                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
