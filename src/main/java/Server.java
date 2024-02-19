import java.net.*;
import java.util.*;
import java.io.*;

//This class is made for start, create thread and close server.
public class Server implements Runnable{
    private ServerSocket serverSocket;
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            while(!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void closeServer() throws IOException {
        for(ClientHandler clientHandler : ClientHandler.clientHandlers) {
            clientHandler.sendMessages("Server is shutting down");
        }
        serverSocket.close();
    }
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1004);
        Server server = new Server(serverSocket);
        System.out.println("The Server is running");
        Thread startServer = new Thread(server);
        startServer.start();
        if(bufferedReader.readLine().equals("close")){
            System.out.println("The Server is shutting down");
            server.closeServer();
            bufferedReader.close();
            System.out.println("shutted");
        }
    }

}

class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private String clientUsername;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedWriter.write("Your username: ");
        bufferedWriter.newLine();
        bufferedWriter.flush();
        this.clientUsername = bufferedReader.readLine();
        clientHandlers.add(this);
        BroadcastMessages("Server: " + clientUsername + " has joined the chat");
        System.out.println("A new member named " + clientUsername + " has joined this ChatRoom");
    }

    public void run() {
        String messageFromClient;
        while(socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient != null){
                    BroadcastMessages(messageFromClient);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void BroadcastMessages(String messageToSend) throws IOException {
        for(ClientHandler clientHandler : clientHandlers) {
            if(clientHandler.socket.isConnected() && clientHandler.clientUsername != clientUsername) {
                clientHandler.bufferedWriter.write(clientUsername + ": " + messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }
        }
    }

    public void sendMessages(String messageToSend) throws IOException {
        bufferedWriter.write(messageToSend);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

}
