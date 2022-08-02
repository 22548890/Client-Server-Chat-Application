import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientGUI implements ActionListener {
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String username;
    //private String strAllMessages="";
    private String msg="";
    private JFrame frame;

    private JTextArea  enteredText;
    private JTextField typedText;
    private DefaultListModel listModel;
    private JList usersList;
    static ClientGUI client; 

    @Override
    public void actionPerformed(ActionEvent e) {
        //get and send text from typedText.getText()
        msg=typedText.getText();
        // strAllMessages+=msg+"\n";
        // enteredText.setText(strAllMessages);
        
        client.sendMessage(msg);

        typedText.setText("");
        typedText.requestFocusInWindow();
        
    }
    
    public ClientGUI(Socket socket, String username) {
        frame = new JFrame();
        //frame.setSize(500, 400);

        JButton btn = new JButton("send");
        btn.addActionListener(this);
        
        enteredText = new JTextArea(10, 32);
        typedText   = new JTextField(32);
        //adding curent username
        //TODO add all 
        listModel = new DefaultListModel();
        listModel.addElement("Online Users:");
        listModel.addElement(username);
        //listModel.remove("")

        usersList = new JList(listModel);
        
        enteredText.setEditable(false);
        usersList.setFocusable(false);
        enteredText.setBackground(Color.LIGHT_GRAY);
        typedText.addActionListener(this);


        Container content = frame.getContentPane();
        content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        content.add(typedText, BorderLayout.SOUTH);
        content.add(usersList, BorderLayout.EAST);
        typedText.requestFocusInWindow();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Client: "+username);
        frame.pack();
        frame.setVisible(true);

        try {
            this.socket = socket;
            
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }

    public void sendMessage(String msg) {
        try{
            

            if(socket.isConnected()) {
                String textToSend = msg;
                Message messageToSend=  null;
                if (textToSend.equals("\\exit")) {
                    closeEverything(socket, objectInputStream, objectOutputStream);
                    System.exit(0);
                } else if (textToSend.startsWith("@")) { // Whisper
                    String str[] = textToSend.split(" ", 2);
                    String clientTo = str[0].substring(1);
                    textToSend = str[1];
                    messageToSend = new Message(textToSend, username, clientTo);
                }
                else if(textToSend.equals("\\addUser")) {//to remove just testing
                   listModel.addElement("New User added");   
                   
                } else {
                    messageToSend = new Message(textToSend, username);
                }
                objectOutputStream.writeObject(messageToSend);
                objectOutputStream.flush();
            }

        }  catch (IOException e) {
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
     }
     public void listenForMessage() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(socket, objectInputStream, objectOutputStream, enteredText);
        Thread thread = new Thread(clientListenerThread);
        thread.start(); //waiting for broadcasted msgs
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

    public static void main(String[] args) throws IOException {
        
        String username= JOptionPane.showInputDialog("Enter your unique username: ");
        String port= JOptionPane.showInputDialog("Enter the port: ", "1234");

        
        Socket socket = new Socket("localhost", Integer.parseInt(port));
        client = new ClientGUI(socket, username);
        client.objectOutputStream.writeObject(new Message(username, username));
        client.objectOutputStream.flush();
        
        client.listenForMessage();
        //client.sendMessage();
    }
}

