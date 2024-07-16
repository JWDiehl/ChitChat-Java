import javax.imageio.IIOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

@SuppressWarnings("serial")
public class SocketClient extends JFrame implements ActionListener, Runnable {
    //Displays Chat History
    JTextArea textArea = new JTextArea();

    //Provides scrolling functionality for textArea
    JScrollPane jp = new JScrollPane(textArea);

    //Input field for entering messages
    JTextField input_Text = new JTextField();

    //Menu Bar
    JMenuBar menuBar = new JMenuBar();

    //The clients socket connection to the server
    Socket sk;

    //Reads incoming messages from the server
    BufferedReader br;

    //Writes outgoing messages to the server
    PrintWriter pw;


    //Constructor --
    public SocketClient() {
        super("Chit Chat");
        setFont(new Font("Arial Black", Font.PLAIN, 12));
        setForeground(new Color(0, 0, 51));
        setBackground(new Color(51, 0, 0));
        textArea.setToolTipText("Chat History");
        textArea.setForeground(new Color(50, 205, 50));
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.BOLD, 13));

        textArea.setBackground(new Color(255, 255, 255));

/*
         JMenu helpMenu = new JMenu("Help");
        JMenuItem update = new JMenuItem("Update Information");
        JMenuItem connect_List = new JMenuItem("Visitor List");

        helpMenu.add(update);
        helpMenu.add(connect_List);

        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
*/
        getContentPane().add(jp, "Center");
        input_Text.setText("Enter your Message:");
        input_Text.setToolTipText("Enter your Message");
        input_Text.setForeground(new Color(0, 0, 0));
        input_Text.setFont(new Font("Tahoma", Font.BOLD, 11));
        input_Text.setBackground(new Color(230, 230, 250));
        
        getContentPane().add(input_Text, "South");
        setSize(325, 411);
        setVisible(true);

        input_Text.requestFocus(); //Place cursor at run time, work after screen is shown

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        input_Text.addActionListener(this); //Event registration
    }

    //Method that prompts user to enter the servers IP address and Nickname using JOptionPane
    public void serverConnection() {
        try {
            String IP = JOptionPane.showInputDialog(this, "Please enter a server IP.", JOptionPane.INFORMATION_MESSAGE);

            //CHANGE 1 == Check if the IP is null
            if (IP == null || IP.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Entered Invalid server IP", "Error", JOptionPane.ERROR_MESSAGE);
                return; //Return if IP is invalid
            }


            //Establishes a socket connection (SK) to the server at an IP Address and port 1234
            sk = new Socket(IP, 1234);

            //CHANGE 2 == CHECK if name is null or empty
            String name = JOptionPane.showInputDialog(this, "Please enter a nickname", JOptionPane.INFORMATION_MESSAGE);
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invalid nickname entered!", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }


/*            while (name.length() > 7) {
                name = JOptionPane.showInputDialog(this, "Please enter a nickname.(7 characters or less)", JOptionPane.INFORMATION_MESSAGE);
            }
*/
            //read incoming messages
            br = new BufferedReader(new InputStreamReader(sk.getInputStream()));

            //writing
            pw = new PrintWriter(sk.getOutputStream(), true);
            pw.println(name); // Send to server side

            new Thread(this).start(); // THread to listen for incoming messages

            //Change 3!!
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(this, "Unknown host: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection error: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new SocketClient().serverConnection(); //Method call at the same time object creation
    }

    //Listens for incoming messages from the server
    @Override
    public void run() {

        try {
            //Change 4
            String data;
            while ((data = br.readLine()) != null) {
                final String message = data;
                //Update textArea with incoming message
                SwingUtilities.invokeLater(() -> {
                    textArea.append(message + "\n"); //textArea Decrease the position of the box's scroll bar by the length of the text entered
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                });
            }
        } catch (IOException e) {
            //Handle IOException (e.g. server disconnect)
            System.out.println("Error reading from server: " + e.getMessage());
        } catch(Exception e){
                System.out.println(e + "Client run fail: " + e.getMessage());
            }
        }

    @Override
    public void actionPerformed(ActionEvent e) {
        String data = input_Text.getText();

        //Change 5
        if (pw != null) {
            pw.println(data); // Send to server side
        } else {
            System.err.println("PrintWriter is null - cant send message.");
        }
        input_Text.setText(""); //Clear input text field after sending message
    }
}