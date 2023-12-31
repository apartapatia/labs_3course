import logging.LoggerSingleton;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers;
    private final Map<ClientHandler, String> clientUsernames;
    private static final Logger logger = LoggerSingleton.getInstance();
    // color
    private static final String ANSI_SEND = "\u001b[48;5;63m";
    private static final String ANSI_SendDirect = "\u001b[48;5;213m";
    public static final String ANSI_RESET = "\u001B[0m";
    // casino instance
    private volatile boolean betIsReady;
    private final ScheduledExecutorService scheduler;
    private Map<String, Integer> betUsersMap = new HashMap<>();
    public final Integer circleMax = 37;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clientHandlers = new ArrayList<>();
        this.clientUsernames = new HashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.betIsReady = false;
    }


    //TODO time management
    public void start() {
        try {
            logger.info("server started. listening on port " + serverSocket.getLocalPort());

            Random random = new Random();
            int randomBetTime = 10 + random.nextInt(30);

            scheduler.scheduleAtFixedRate(this::openBetting, randomBetTime, randomBetTime, TimeUnit.SECONDS);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("client connected from: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                var clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();

            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "an error occurred in start", e);
        }

    }

    //TODO process bet

    private void betWinner(){
        Random random = new Random();
        boolean winnerUser = false;
        int winnerBet = random.nextInt(circleMax);
            for (Map.Entry<String, Integer> entry : betUsersMap.entrySet()){
                if (entry.getValue().equals(winnerBet)){
                    winnerUser = true;
                    broadcastMessage("winner " + entry.getKey(), "server");
                }
            }
            if (!winnerUser) {
                broadcastMessage("winner bet is " + winnerBet, "server");
            }
    }

    private void openBetting() {
        if (!betIsReady) {
            broadcastMessage("betting open", "server");
            betIsReady = true;
            scheduler.schedule(this::closeBetting, 10, TimeUnit.SECONDS);
        }
    }

    private void closeBetting() {
        betWinner();
        betUsersMap.clear();
        broadcastMessage("betting closed", "server");
        betIsReady = false;
    }

    // work with clients
    public synchronized void broadcastMessage(String message, String sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(ANSI_SEND +  " " + sender + ": " + message + " " + ANSI_RESET);
        }
    }
    public synchronized void broadcastUserJoinMessage(String username) {
        String message = "user " + username + " has joined the chat.";
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(ANSI_SEND + " " + "server" + ": " + message + " " + ANSI_RESET);
        }
    }

    public synchronized void broadcastUserLeaveMessage(String username) {
        String message = "user " + username + " has left the chat.";
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(ANSI_SEND + " " + "server" + ": " + message + " " + ANSI_RESET);
        }
    }

    public synchronized void broadcastNameChangeMessage(String oldUsername, String newUsername) {
        String message = "user " + oldUsername + " has changed the name to " + newUsername + ".";
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(ANSI_SEND + " " + "server" + ": " + message + " " + ANSI_RESET);
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
        broadcastUserLeaveMessage(removedUsername);
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
                var reader = new BufferedReader(new InputStreamReader(inputStream));

                String username = reader.readLine();
                System.out.println("client identified as: " + username);
                while (server.clientUsernames.containsValue(username)){
                    sendMessage("your name is repeated, so change it.\n");
                    username = reader.readLine();
                }
                server.associateUsername(this, username);
                server.broadcastUserJoinMessage(username);

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
                        server.broadcastNameChangeMessage(username, message.split(" ")[1]);
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
                    } else if (message.startsWith("@bet")) {
                        String[] parts = message.split(" ");
                        if (parts.length != 2){
                            sendMessage("usage : <@bet> <numberBet>");
                        } else {
                            if (server.betIsReady) {
                                server.betUsersMap.put(username, Integer.valueOf(parts[1]));
                            }
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