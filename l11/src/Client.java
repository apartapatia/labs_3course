import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {

    private static final String COMMAND_NAME = "@name";
    private static final String LOGIN_MESSAGE = " has logged in.";
    private static final String LOGOUT_MESSAGE = " has logged out.";
    private static String currentUsername = " ";
    private static boolean shouldExit = false;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage : java Client <server_host> <server_port>");
            System.exit(1);
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try {
            InetAddress serverAddress = InetAddress.getByName(serverHost);

            DatagramSocket socket = new DatagramSocket();
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your name: ");
            currentUsername = userInput.readLine();

            sendLoginNotification(socket, serverHost, serverPort);


            Thread receiveThread = new Thread(new MessageReceiver(socket));
            System.out.println("type '@name' to change a name");
            System.out.println("type '@quit' to exit");
            receiveThread.start();

            while (true) {
                System.out.print("Enter a message: ");
                String message = userInput.readLine();

                if ("@quit".equals(message)) {
                    System.out.println("Exiting chat.");
                    shouldExit = true;
                    sendQuitNotification(socket, serverHost, serverPort);
                    break;
                } else if (message.startsWith(COMMAND_NAME)) {
                    handleNameChange(socket, serverAddress, serverPort, message);
                } else {
                    sendMessage(socket, serverAddress, serverPort, message);
                }
            }
            if (!socket.isClosed()) {
                socket.close();
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendLoginNotification(DatagramSocket socket, String serverHost, int serverPort) throws IOException {
        String loginMessage = currentUsername + LOGIN_MESSAGE;
        byte[] loginData = loginMessage.getBytes();
        DatagramPacket loginPacket = new DatagramPacket(loginData, loginData.length,
                InetAddress.getByName(serverHost), serverPort);
        socket.send(loginPacket);
    }

    private static void sendQuitNotification(DatagramSocket socket, String serverHost, int serverPort) throws IOException {
        String quitMessage = currentUsername + LOGOUT_MESSAGE;
        byte[] loginData = quitMessage.getBytes();
        DatagramPacket loginPacket = new DatagramPacket(loginData, loginData.length,
                InetAddress.getByName(serverHost), serverPort);
        socket.send(loginPacket);
    }


    private static void handleNameChange(DatagramSocket socket, InetAddress serverAddress, int serverPort, String message) throws IOException {
        currentUsername = message.substring(COMMAND_NAME.length()).trim();
        System.out.println("Your new name is: " + currentUsername);
        String messageNewUsername = "@name " + currentUsername;
        byte[] sendData = messageNewUsername.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                serverAddress, serverPort);
        socket.send(sendPacket);
    }

    private static void sendMessage(DatagramSocket socket, InetAddress serverAddress, int serverPort, String message) throws IOException {
        if (!message.isEmpty()) {
            String messageWithUsername = currentUsername + ": " + message;
            byte[] sendData = messageWithUsername.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    serverAddress, serverPort);
            socket.send(sendPacket);
        }
    }

    static class MessageReceiver implements Runnable {
        private final DatagramSocket socket;

        public MessageReceiver(DatagramSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                while (!shouldExit) {
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
