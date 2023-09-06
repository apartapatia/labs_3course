import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {

    private static final String COMMAND_NAME = "@name";
    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Usage : java Server <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        try {
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("Server is running on port " + port);

            while (true){
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData,
                        receiveData.length);
                socket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0,
                        receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                Thread messageHandlerThread = new Thread(new MessageHandler(socket, message, clientAddress, clientPort));
                messageHandlerThread.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    static class MessageHandler implements Runnable{
        private final DatagramSocket socket;
        private final String message;
        private final InetAddress clientAddress;
        private final int clientPort;

        public MessageHandler(DatagramSocket socket, String message, InetAddress clientAddress, int clientPort){
            this.socket = socket;
            this.message = message;
            this.clientPort = clientPort;
            this.clientAddress = clientAddress;
        }

        @Override
        public void run() {
            if (message.startsWith(COMMAND_NAME)){
                handleNameChange();
            } else {
                handleRegularMessage();
            }
        }
        private void handleNameChange(){
            String[] parts = message.split(" ");
            if (parts.length >= 2){
                String newUserName = parts[1];
                System.out.println("[" + clientAddress + ":" + clientPort + "]" + " - User set name to " + newUserName);
            }
        }
        private void handleRegularMessage(){
            System.out.println("[" + clientAddress + ":" + clientPort + "]" + " - " + message);
        }
    }
}