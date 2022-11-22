package socketClient;

import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("ip주소", port);
            Thread thread = new Receiver(socket);
            thread.start();
        }
        catch (Exception e) {
            System.err.println("에러발생");
            e.printStackTrace();
        }
    }
}
