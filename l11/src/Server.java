import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

public class Server {
    private final DatagramSocket datagramSocket;
    private byte[] buffer;
    private InetAddress clientAddress;
    private int clientPort;
    private String username = "server";
    private int secretNumber;
    private boolean gameStarted = false;
    private int minRange = 1;
    private int maxRange = 100;

    public Server(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
        this.secretNumber = new Random().nextInt(maxRange) + minRange;
    }

    public void receiveMessages() {
        try {
            while (true) {
                this.buffer = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                this.clientAddress = datagramPacket.getAddress();
                this.clientPort = datagramPacket.getPort();

                String messageFromClient = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                if (Objects.equals((messageFromClient.split(" ")[1]), "@game")){
                    System.out.println(secretNumber);
                    gameStarted = true;
                } else if (gameStarted){
                    processGuess(messageFromClient.split(" ")[1]);
                } else {
                    System.out.println(messageFromClient);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException {
        message = this.username + ": " + message;
        byte[] messageBytes = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, this.clientAddress, this.clientPort);
        datagramSocket.send(sendPacket);
    }

    public void sendQuitNotification() throws IOException {
        String quitMessage = "server has shutdown!";
        byte[] messageBytes = quitMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, this.clientAddress, this.clientPort);
        datagramSocket.send(sendPacket);
    }

    //@game methods
    public void processGuess(String guess) throws IOException {
        try {
            int guessedNumber = Integer.parseInt(guess);
            if (guessedNumber == secretNumber) {
                sendMessage("Congratulations! You guessed the number.");
                resetGame();
            } else if (guessedNumber < secretNumber) {
                sendMessage("Too low! Try a higher number.");
            } else {
                sendMessage("Too high! Try a lower number.");
            }
        } catch (NumberFormatException e) {
            sendMessage("Invalid input. Please enter a valid number.");
        }
    }
    private void resetGame() throws IOException {
        this.secretNumber = new Random().nextInt(100) + 1;
        this.gameStarted = false;
        sendMessage("Game has been reset. Type '@game' to begin a new game.");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage : java Server <port>");
            System.exit(-1);
        }

        try {
            DatagramSocket datagramSocket = new DatagramSocket(Integer.parseInt(args[0]));
            Server server = new Server(datagramSocket);
            System.out.println("server is start on port " + Integer.parseInt(args[0]));


            //start thread to send messages
            Thread sendThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String messageToClient = scanner.nextLine().trim();

                    if (messageToClient.isEmpty()) {
                        System.out.println("Message cannot be empty, please enter a message:");
                        continue;
                    }
                    try {
                        if (messageToClient.startsWith("@name") ){
                            if (messageToClient.split(" ").length == 1){
                                System.out.println("name is empty, try again!");
                            } else {
                                server.username = messageToClient.split(" ")[1];
                            }
                        } else if (Objects.equals(messageToClient.split(" ")[0], "@quit")) {
                            server.sendQuitNotification();
                            System.out.println("server has shutdown");
                            break;
                        } else {
                            server.sendMessage(messageToClient);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } if (!datagramSocket.isClosed()){
                    datagramSocket.close();
                } System.exit(0);
            });

            sendThread.start();

            //start thread to receive messages
            server.receiveMessages();
        } catch (NumberFormatException | IOException e) {
            System.out.println("error: " + e.getMessage());
            System.out.println("invalid <port> arguments");
        }
    }
    public int getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }

    public int getMinRange() {
        return minRange;
    }

    public void setMinRange(int minRange) {
        this.minRange = minRange;
    }
}