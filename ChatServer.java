import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcasts messages to all clients
    public static void broadcast(String message, ClientHandler excludeUser) {
        for (ClientHandler aUser : clientHandlers) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }

    // Removes a client
    public static void removeUser(ClientHandler user) {
        clientHandlers.remove(user);
        System.out.println("User disconnected");
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private String userName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
        ) {
            writer = new PrintWriter(output, true);

            // Get username
            writer.println("Enter your name:");
            userName = reader.readLine();
            ChatServer.broadcast(userName + " has joined the chat", this);

            String clientMessage;

            while ((clientMessage = reader.readLine()) != null) {
                String serverMessage = "[" + userName + "]: " + clientMessage;
                System.out.println(serverMessage);
                ChatServer.broadcast(serverMessage, this);
            }
        } catch (IOException e) {
            System.out.println("Error in client handler: " + e.getMessage());
        } finally {
            ChatServer.removeUser(this);
            ChatServer.broadcast(userName + " has left the chat.", this);
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    void sendMessage(String message) {
        writer.println(message);
    }
}
