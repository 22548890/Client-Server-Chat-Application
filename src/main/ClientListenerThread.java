import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class ClientListenerThread implements Runnable {

    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private JTextArea enteredText;
    private DefaultListModel listModel;
    private JFrame frame;
    private boolean firstConnection;


    public ClientListenerThread(Socket socket, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, JTextArea enteredText, DefaultListModel listModel, JFrame frame) {
        this.socket = socket;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
        this.enteredText = enteredText;
        this.listModel = listModel;
        this.frame = frame;
        this.firstConnection = true;
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try{
                printMessage((Message) objectInputStream.readObject());
            } catch (IOException e) {
                closeEverything();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void printMessage(Message message) {
        String msg = message.from();
        if (message.from().equals("SERVER") && message.text().endsWith("has entered the chat!")) {
            if (firstConnection) {
                frame.setTitle("Client: " + message.text().split(" has entered the chat" ,2)[0]);

                try {
                    String[] ls = (String[]) objectInputStream.readObject();
                    for (String name : ls) {
                        listModel.addElement(name);
                    }
                } catch (ClassNotFoundException e) {
                    closeEverything();
                } catch (IOException e) {
                    closeEverything();
                }
                firstConnection = false;
            } else {
                listModel.addElement(message.text().split(" has entered the chat" ,2)[0]);
            }
        } else if (message.from().equals("SERVER") && message.text().endsWith("has left the chat!"))  {
            listModel.removeElement(message.text().split(" has left the chat" ,2)[0]);
        }
        if (message.to() != null && !message.from().equals("SERVER")) {
            msg += " whispers to " + message.to();
        }
        msg += ": " + message.text();
        System.out.println(msg);
        enteredText.insert(msg + "\n", enteredText.getText().length());
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

        System.out.println("SERVER: Shut down");
        enteredText.insert("SERVER: Shut down" + "\n", enteredText.getText().length());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        System.exit(0);
    }
}
