import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Client extends JFrame {
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String username;
    static JTextArea ta= new JTextArea();
    static JTextField tf = new JTextField(10);
    static String txt;
    
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            
            this.username = username;
             
        } catch (IOException e) {
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }

    public void sendMessage() {
        try{
            objectOutputStream.writeObject(new Message(username, username));
            objectOutputStream.flush();

            Scanner scanner = new Scanner(System.in);
        
            while(socket.isConnected()){

                String textToSend = scanner.next();
                Message messageToSend=  null;
                if (textToSend.equals("\\exit")) {
                    closeEverything(socket, objectInputStream, objectOutputStream);
                    System.exit(0);
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
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
     }
     public void listenForMessage() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(socket, objectInputStream, objectOutputStream);
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
        // GUI

        //Creating the Frame
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        //Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("Help");
        
        mb.add(m1);
        JMenuItem m11 = new JMenuItem("Type '\\exit' to exit client");
        JMenuItem m22 = new JMenuItem("Type '@username' to whisper to username");
        m1.add(m11);
        m1.add(m22);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JLabel label = new JLabel("Enter chat");
        //JTextField tf = new JTextField(10); // accepts up to 10 characters
        JButton send = new JButton("Send");
        JButton btnClear = new JButton("Clear");
        
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(send);
        panel.add(btnClear); 

        // Text Area at the Center
        //JTextArea ta = new JTextArea();

        send.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String txt = tf.getText();
                        ta.append(txt + "\n");
                        
                    }
                });

                    btnClear.addActionListener(new ActionListener() {
            
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ta.setText("");
                            tf.setText("");
                        }
                    });

        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, ta);
        frame.setVisible(true);

            String username= JOptionPane.showInputDialog("Enter your username: ");
            int port = Integer.parseInt(JOptionPane.showInputDialog("Enter the port number: ", "1234"));
        ////// GUI

        Socket socket = new Socket("localhost", port);
        Client client = new Client(socket, username);
        client.listenForMessage();
        client.sendMessage();
    }
}
