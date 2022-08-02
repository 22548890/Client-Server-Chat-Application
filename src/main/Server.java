import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;
    
    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.checkUsername();
                System.out.println("New Client Connected!");

                Thread thread = new Thread(clientHandler);
                thread.start();

            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port_number>");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        if (port < 1 || port > 65535) {
            System.out.println("Port number must be between 1 and 65535");
            System.exit(0);
        }

        ServerSocket serverSocket = new ServerSocket(port);
        Server server = new Server(serverSocket);
        server.startServer();
        
    }
}
