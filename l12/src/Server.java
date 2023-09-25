import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers;
    private final Map<ClientHandler, String> clientUsernames;
    private static final String ANSI_SEND = "\u001b[48;5;63m";
    private static final String ANSI_SendDirect = "\u001b[48;5;213m";
    public static final String ANSI_RESET = "\u001B[0m";
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clientHandlers = new ArrayList<>();
        this.clientUsernames = new HashMap<>();
    }

    public void start() {
        try {
            logger.info("Server started. Listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected from: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "an error occurred in start", e);
        }
    }

    public synchronized void broadcastMessage(String message, String sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(ANSI_SEND +  " " + sender + ": " + message + " " + ANSI_RESET);
        }
    }

    public synchronized void associateUsername(ClientHandler clientHandler, String username) {
        clientUsernames.put(clientHandler, username);
    }

    public synchronized String getUsername(ClientHandler clientHandler) {
        return clientUsernames.get(clientHandler);
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        String removedUsername = clientUsernames.remove(clientHandler);
        broadcastMessage("user " + removedUsername + " has left the chat.", "server");
        System.out.println("client disconnected: " + removedUsername);
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final Server server;
        private final PrintWriter writer;
        private boolean userOffServer = false;

        public ClientHandler(Socket clientSocket, Server server) throws IOException {
            this.clientSocket = clientSocket;
            this.server = server;
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String username = reader.readLine();
                System.out.println("client identified as: " + username);
                while (server.clientUsernames.containsValue(username)){
                        sendMessage("your name is repeated, so change it.\n");
                        username = reader.readLine();
                }
                server.associateUsername(this, username);
                server.broadcastMessage("user " + username + " has join the chat.", "server");

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.isEmpty()) {
                        sendMessage("message cannot be empty, please enter a message:");
                        continue;
                    }
                    if (message.equals("@quit")){
                        server.removeClient(this);
                        userOffServer = true;
                        clientSocket.close();
                        break;
                    } else if (message.startsWith("@name")) {
                        server.broadcastMessage("user " + username + " has change the name on " + message.split(" ")[1], "server");
                        server.associateUsername(this, message.split(" ")[1]);
                    } else if (message.startsWith("@senduser")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length != 3){
                            sendMessage("usage : <@senduser> <targetName> <message>");
                        } else {
                            String targetName = parts[1];
                            String messageToSend = parts[2];
                            sendDirectMessage(targetName, messageToSend);
                        }
                    } else {
                        server.broadcastMessage(message, server.getUsername(this));
                    }

                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "an error occurred in run", e);
            } finally {
                try {
                    clientSocket.close();
                    if (!userOffServer){
                        server.removeClient(this);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "an error occurred in run", e);
                }
            }
        }
        public void sendMessage(String message) {
            writer.println(message);
        }

        private void sendDirectMessage(String targetName, String messageToSend) {
            for (ClientHandler clientHandler : server.clientHandlers){
                if (server.getUsername(clientHandler).equals(targetName)){
                    clientHandler.sendMessage(ANSI_SendDirect + " " + "(private) " + server.getUsername(this)
                            + ": " + messageToSend + " " + ANSI_RESET);
                    return;
                }
            }
            sendMessage("user " + targetName + " not found.");
        }


    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: java Server <port>");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            ServerSocket serverSocket = new ServerSocket(port);
            Server server = new Server(serverSocket);
            server.start();
        } catch (NumberFormatException | IOException e) {
            logger.log(Level.SEVERE, "invalid port number or unable to start server: ", e);
        }
    }
}