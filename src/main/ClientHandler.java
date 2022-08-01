import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
           
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            this.clientUsername = ((Message) objectInputStream.readObject()).text();//waits for message to be sent
            clientHandlers.add(this);
            sendMessage(new Message(clientUsername + " has entered the chat!","SERVER"));
        } catch (IOException e){
            closeEverything(socket, objectInputStream, objectOutputStream);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // run on every thread
        //thread waiting and sending for each message
        Message messageFromClient;

        while (socket.isConnected()) {
            try{
                messageFromClient = (Message) objectInputStream.readObject();
                if (messageFromClient != null) {
                    if (messageFromClient.text().equals("\\list")) {
                        String text = "List of current users -\n";
                        for (ClientHandler handler : clientHandlers) {
                            text += "        " + handler.clientUsername + "\n";
                        }
                        text = text.substring(0, text.length()-1);
                        Message msg = new Message(text, "SERVER", clientUsername);
                        sendMessage(msg);
                    } else {
                        sendMessage(messageFromClient);
                    }
                }
            } catch (IOException e){
                closeEverything(socket, objectInputStream, objectOutputStream);
                break; //exit while
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }

    }

    public void sendMessage(Message messageToSend) {
        for (ClientHandler clientHandler : clientHandlers){
            try{
                if (messageToSend.to() == null) {   // broadcast
                    clientHandler.objectOutputStream.writeObject(messageToSend);
                    clientHandler.objectOutputStream.flush();//manual clear before it fills
                } else {    // whisper
                    if (clientHandler.clientUsername.equals(messageToSend.to()) || clientHandler.clientUsername.equals(clientUsername)){
                        clientHandler.objectOutputStream.writeObject(messageToSend);
                        clientHandler.objectOutputStream.flush();//manual clear before it fills
                    }
                }
            } catch (IOException e){
                closeEverything(socket, objectInputStream, objectOutputStream);
            }
        }
    }

    public void closeEverything(Socket socket, ObjectInputStream ois, ObjectOutputStream ous) {
        removeClientHandler();
        try {
            if (ois != null) {
                ois.close();
            }

            if (ous != null) {
                ous.close();
            }

            if (socket != null) {
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        sendMessage(new Message(clientUsername + " has left the chat!","SERVER"));
        System.out.println("Client Disconnected!");
    }

}
