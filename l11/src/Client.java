import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Scanner;

public class Client {

    private final DatagramSocket datagramSocket;
    private final InetAddress serverAddress;
    private final int serverPort;
    private byte[] buffer;
    private String username = "client";
    private static final String LOGIN_MESSAGE = " has logged in.";
    private static final String LOGOUT_MESSAGE = " has logged out.";

    public Client(DatagramSocket datagramSocket, InetAddress serverAddress, int serverPort) {
        this.datagramSocket = datagramSocket;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void sendMessage(String message) throws IOException {
        message = this.username + ": " + message;
        buffer = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, this.serverAddress, this.serverPort);
        datagramSocket.send(sendPacket);
    }

    public void receiveMessages() {
        try {
            while (true) {
                buffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(receivePacket);
                String messageFromServer = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println(messageFromServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            datagramSocket.close();
        }
    }
    public void sendLoginNotification() throws IOException {
        String loginMessage = this.username + LOGIN_MESSAGE;
        byte[] loginData = loginMessage.getBytes();
        DatagramPacket loginPacket = new DatagramPacket(loginData, loginData.length, this.serverAddress, this.serverPort);
        datagramSocket.send(loginPacket);
    }

    public void sendQuitNotification() throws IOException {
        String quitMessage = this.username + LOGOUT_MESSAGE;
        byte[] loginData = quitMessage.getBytes();
        DatagramPacket loginPacket = new DatagramPacket(loginData, loginData.length, this.serverAddress, this.serverPort);
        datagramSocket.send(loginPacket);
    }
    public void sendChangeUsernameNotification() throws IOException {
        String usernameMessage = "client set name to - " + this.username;
        byte[] loginData = usernameMessage.getBytes();
        DatagramPacket loginPacket = new DatagramPacket(loginData, loginData.length, this.serverAddress, this.serverPort);
        datagramSocket.send(loginPacket);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("usage : java Client <host> <port>");
            System.exit(-1);
        }

        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(args[0]);
            int serverPort = Integer.parseInt(args[1]);
            Client client = new Client(datagramSocket, serverAddress, serverPort);

            client.sendLoginNotification();

            //start thread to receive messages
            Thread receiveThread = new Thread(client::receiveMessages);
            System.out.println("type '@name' to change a name");
            System.out.println("type '@quit' to exit");
            System.out.println("type '@game' to play predict number game");
            receiveThread.start();


            //start thread to send messages
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String messageToSend = scanner.nextLine().trim();
                if (messageToSend.isEmpty()) {
                    System.out.println("message cannot be empty, please enter a message:");
                    continue;
                }
                if (messageToSend.startsWith("@name")) {
                    if (messageToSend.split(" ").length == 1){
                        System.out.println("name is empty, try again!");
                    } else {
                        client.username = messageToSend.split(" ")[1];
                        client.sendChangeUsernameNotification();
                    }
                } else if (Objects.equals(messageToSend.split(" ")[0], "@quit")) {
                    client.sendQuitNotification();
                    System.out.println("exiting chat!");
                    break;
                } else if (Objects.equals(messageToSend.split(" ")[0], "@game")) {
                    client.sendMessage("@game");
                    String predict = scanner.nextLine().trim();
                    client.sendMessage(predict);
                } else {
                    client.sendMessage(messageToSend);
                }
            } if (!datagramSocket.isClosed()){
                datagramSocket.close();
            }
            System.exit(0);

        } catch (NumberFormatException | IOException e) {
            System.out.println("error: " + e.getMessage());
            System.out.println("invalid <port> arguments");
        }
    }
}