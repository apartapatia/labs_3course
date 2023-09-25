import io.LoggerSingleton;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final Logger logger = LoggerSingleton.getInstance();
    private final String serverAddress;
    private final int serverPort;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void start() {
        try (Socket socket = new Socket(serverAddress, serverPort))
        {
            /* last version
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("connected to server.");
            System.out.println("write your username: "); */

            logger.info("connected to server.");
            System.out.println("write your username\n(type '@quit' to exit or type '@senduser' to send private message):");

            Thread readThread = new Thread(new ServerReader(socket));
            readThread.start();

            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while ((message = userInputReader.readLine()) != null) {
                if (message.equals("@quit")){
                    break;
                } else {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println(message);
                }
            }
        } catch (IOException e) {
            System.exit(0);
            //logger.log(Level.SEVERE, "an error occurred in start", e);
        }
        //System.exit(0);
    }

    private record ServerReader(Socket socket) implements Runnable {
        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.exit(0);
                //logger.log(Level.SEVERE, "an error occurred in ServerReader", e);
            }
        }
    }
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println("usage: java Client <server_address> <server_port>");
                return;
            }
            String serverAddress = args[0];
            int serverPort = Integer.parseInt(args[1]);

            Client client = new Client(serverAddress, serverPort);
            client.start();
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "invalid port number or unable to start server: ", e);
        }
    }
}