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
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001b[38;5;49m";


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
                    sendMessage("game was started!");
                    gameStarted = true;
                    sendGameMessage("rules : ask questions to the server to find out if your number is higher or lower than the number guessed by the server \nprint l or h for predict the number");
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

    public void sendGameMessage(String message) throws IOException {
        String coloredMessage = ANSI_BLUE + message + ANSI_RESET;
        byte[] messageBytes = coloredMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, this.clientAddress, this.clientPort);
        datagramSocket.send(sendPacket);
    }

    //@game methods
    public void processGuess(String guess) throws IOException {
        int guessedNumber = minRange + (maxRange - minRange + 1) / 2;
        sendGameMessage(guessedNumber + " - your predict number");
        try {
            if (guessedNumber == secretNumber) {
                sendGameMessage("congratulations! you guessed the number.");
                resetGame();
            } else if (guess.equals("h")) {
                if (guessedNumber > secretNumber){
                    sendGameMessage("yes, your number higher.");
                    setMaxRange(guessedNumber - 1);
                } else {
                    sendGameMessage("no, your number lower .");
                }
            } else if (guess.equals("l")) {
                if (guessedNumber < secretNumber){
                    sendGameMessage("yes, your number lower.");
                    setMinRange(guessedNumber + 1);
                } else {
                    sendGameMessage("no, your number higher.");
                }
            } else {
                sendGameMessage("invalid input. please enter 'h' for too high or 'l' for too low.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void resetGame() throws IOException {
        setMinRange(1);
        setMaxRange(100);
        this.secretNumber = new Random().nextInt(getMaxRange()) + getMinRange();
        this.gameStarted = false;
        sendGameMessage("game has been reset. type '@game' to begin a new game.");
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
                        System.out.println("message cannot be empty, please enter a message:");
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