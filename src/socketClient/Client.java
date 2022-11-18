package socketClient;

import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("172.16.101.162", 1234);
            Thread thread = new Receiver(socket);
            thread.start();
        }
        catch (Exception e) {
            System.err.println("에러발생");
            e.printStackTrace();
        }
    }
}
