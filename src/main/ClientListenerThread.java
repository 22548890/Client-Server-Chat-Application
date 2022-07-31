import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientListenerThread implements Runnable {

    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public ClientListenerThread(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) {
        this.socket = socket;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try{
                printMessage((Message) objectInputStream.readObject());
            } catch (IOException e) {
                closeEverything(socket, objectInputStream, objectOutputStream);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void printMessage(Message message) {
        String msg = message.from();
        if (message.to() != null) {
            msg += " whispers to " + message.to();
        }
        msg += ": " + message.text();
        System.out.println(msg);
    }

    public void closeEverything(Socket socket, ObjectInputStream ois, ObjectOutputStream ous) {  
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
}