import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers;
    private final Map<ClientHandler, String> clientUsernames;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clientHandlers = new ArrayList<>();
        this.clientUsernames = new HashMap<>();
    }

    public void start() {
        try {
            System.out.println("server started. listening on port " + serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("client connected from: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void broadcastMessage(String message, String sender) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(sender + ": " + message);
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

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.equals("@quit")){
                        server.removeClient(this);
                        userOffServer = true;
                        clientSocket.close();
                        break;
                    }

                    server.broadcastMessage(message, server.getUsername(this));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    if (!userOffServer){
                        server.removeClient(this);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
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
            System.err.println("invalid port number or unable to start server: " + e.getMessage());
        }
    }
}