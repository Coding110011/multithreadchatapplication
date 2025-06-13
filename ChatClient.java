import java.io.*;
import java.net.*;

public class ChatClient {
    private String hostname = "localhost";
    private int port = 12345;
    private String userName;

    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);

            System.out.println("Connected to the chat server");

            new ReadThread(socket).start();
            new WriteThread(socket, this).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return this.userName;
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.execute();
    }
}

class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;

    public ReadThread(Socket socket) {
        this.socket = socket;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                System.out.println(response);
            } catch (IOException ex) {
                System.out.println("Connection closed.");
                break;
            }
        }
    }
}

class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        Console console = System.console();

        System.out.print("Enter your name: ");
        String userName = console.readLine();
        client.setUserName(userName);
        writer.println(userName);

        String text;

        while (true) {
            text = console.readLine();
            writer.println(text);
        }
    }
}
