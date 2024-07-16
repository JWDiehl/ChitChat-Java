import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketServer {
    ServerSocket server;
    InetAddress addr;
    
    ArrayList<ServerThread> list = new ArrayList<>();

    public SocketServer() {
        try {
        	addr = InetAddress.getByName("127.0.0.1");
        	//addr = InetAddress.getByName("192.168.43.1");
            
        	server = new ServerSocket(1234,50, addr); // Server Socket initialized with IP and Port
            System.out.println("\n Waiting for Client connection");
            SocketClient.main(null);
            while(true) {
                Socket sk = server.accept(); //Waits for a client connection
                System.out.println(sk.getInetAddress() + " connect");

                //Thread connected clients to ArrayList
                ServerThread st = new ServerThread(this, sk);
                addThread(st); // Add thread to list
                st.start(); // Start thread
            }
        } catch(IOException e) {
            System.out.println(e + "-> ServerSocket failed");
        }
    }

    //Method to add thread to list
    public void addThread(ServerThread st) {
        list.add(st);
    }

    //Method to remove a thread from the list
    public void removeThread(ServerThread st){
        list.remove(st); //remove
    }

    //Method to broadcast message to all clients
    public void broadCast(String message){
        for(ServerThread st : list){
            st.sendMessage(message);
        }
    }

    //Method to start the server
    public static void main(String[] args) {
        new SocketServer();
    }
}

class ServerThread extends Thread {
    SocketServer server;
    Socket sk;

    //commented out
    //PrintWriter pw;
    String name;

    //Constructor to initialize thread with server and client socket
    public ServerThread(SocketServer server, Socket sk) {

        this.server = server;
        this.sk = sk;
    }

    @Override
    public void run() {
        try {
            // read incoming messages to client
            BufferedReader br = new BufferedReader(new InputStreamReader(sk.getInputStream()));

            // writing to send messages to client
            PrintWriter pw = new PrintWriter(sk.getOutputStream(), true);
            name = br.readLine(); //read clients name
            server.broadCast("**["+name+"] Entered**"); //broadcast clients entry

            String data;
            while((data = br.readLine()) != null ){
                if("/list".equals(data)){ //Correct compare string with equals()
                    pw.println("a"); // respond to list command
                }
                server.broadCast("["+name+"] "+ data); //broadcast message to all clients
            }
        } catch (IOException e) {
            //Remove the current thread from the ArrayList.
            server.removeThread(this);
            server.broadCast("**["+name+"] Left**");
            System.out.println(sk.getInetAddress()+" - ["+name+"] Exit");
            System.out.println(e + "---->");
        }
    }

    //Method to send a message to the client
    public void sendMessage(String message) {
        try {
            PrintWriter pw = new PrintWriter(sk.getOutputStream(), true);
            pw.println(message); //send msg to client
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
        }
    }
}