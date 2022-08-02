import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String username;
    
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            
            this.username = username;
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void sendMessage() {
        try{
            objectOutputStream.writeObject(new Message(username, username));
            objectOutputStream.flush();

            Scanner scanner = new Scanner(System.in);

            while(socket.isConnected()){
                String textToSend = scanner.nextLine();
                Message messageToSend=  null;
                if (textToSend.equals("\\exit")) {
                    closeEverything();
                } else if (textToSend.startsWith("@")) { // Whisper
                    String str[] = textToSend.split(" ", 2);
                    String clientTo = str[0].substring(1);
                    textToSend = str[1];
                    messageToSend = new Message(textToSend, username, clientTo);
                } else {
                    messageToSend = new Message(textToSend, username);
                }
                objectOutputStream.writeObject(messageToSend);
                objectOutputStream.flush();
            }

        }  catch (IOException e) {
            closeEverything();
        }
     }
     public void listenForMessage() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(socket, objectInputStream, objectOutputStream);
        Thread thread = new Thread(clientListenerThread);
        thread.start(); //waiting for broadcasted msgs
     }

     
    public void closeEverything() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
        } catch (IOException e) {}

        try {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
        } catch (IOException e) {}

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {}

        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java Client <host_IP_address> <port_number>");
            System.exit(0);
        }
        Socket socket= null;
        try {
            socket = new Socket(args[0], Integer.parseInt(args[1]));
        } catch (Exception e) {
            System.out.println("Invalid IP address or invalid port number");
            System.exit(0);
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        Client client = new Client(socket, username);
        
        client.listenForMessage();
        client.sendMessage();
    }
}
