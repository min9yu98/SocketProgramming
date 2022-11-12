import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final static int SERVER_PORT = 1234;

    public static void main(String args[]){
        try {
            //서버 소켓 생성
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            Unit unit = Unit.getInstance();
            unit.setFood("치킨");
            unit.setAmount(1000);
            System.out.println("Server Start");
            while(true){
                Socket socket = serverSocket.accept();
                Receiver receiver = new Receiver(socket, unit);
                receiver.start();
//            int count = 0;
//            Thread[] thread = new Thread[10];
//            while (true) {
//                socket = serverSocket.accept();
//                thread[count] = new Thread(new Receivers(user, socket));
//                thread[count].start();
//                count++;
//            }

//                String name = unit.getFood();
//                int amount = unit.getAmount();
//                System.out.println("음식: " + name + "\n수량 : " + amount);
//                objectOutputStream.writeBytes("음식: " + name + "\n수량 : " + amount);
                //사용자 추가해줍니다.
//                user.addClient(name, socket);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
