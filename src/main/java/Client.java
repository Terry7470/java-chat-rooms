import java.io.*;
import java.net.*;

public class Client implements Runnable {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedReader bufferedReaderFromTerminal;
    private BufferedWriter bufferedWriter;
    private String username;
    private int numbers;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedReaderFromTerminal = new BufferedReader(new InputStreamReader(System.in));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.numbers = -1;
    }

    public void sendMessages() throws IOException {
        String messageToSend;
        while (socket.isConnected()) {
            messageToSend = bufferedReaderFromTerminal.readLine();
            if (!messageToSend.equals("!close")) {
                numbers++;
                if (numbers == 0) {
                    username = messageToSend;
                }
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                close();
                break;
            }
        }
    }

    public void run() {
        try {
            String messageReceived;
            while(socket.isConnected()) {
                messageReceived = bufferedReader.readLine();
                if(messageReceived != null) {
                    System.out.println(messageReceived);
                }
            }
        } catch (IOException e) {
            try {
                socket.close();
                bufferedWriter.close();
                bufferedReader.close();
                bufferedReaderFromTerminal.close();
            } catch (IOException ex) {
                try {
                    System.out.println("disconnected");
                    close();
                } catch (IOException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }
    }

    public void close() throws IOException {
        bufferedReaderFromTerminal.close();
        bufferedWriter.close();
        bufferedReader.close();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1004);
            Client client = new Client(socket);
            Thread listenForMessage = new Thread(client);
            listenForMessage.start();
            client.sendMessages();
        } catch (UnknownHostException e) {
            System.out.println("Wrong IP");
        } catch (IOException e) {
            System.out.println("Internet error or server closed");
        }

    }
}
