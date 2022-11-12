import java.io.*;
import java.net.Socket;

public class Receiver extends Thread implements Runnable {
    Socket socket = null;
    Unit unit = null;
    public Receiver(Socket socket, Unit unit) {
        this.socket = socket;
        this.unit = unit;
    }

    @Override
    public void run() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);  // 보조 스트림이라 기반 스트림이 필요
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);  // 보조 스트림이라 기반 스트림이 필요
            while (true) {
                // 현재 메뉴 상황 전송
                objectOutputStream.writeObject(unit);

                // 고개으로부터 주문 정보 수신
                Order order = (Order) objectInputStream.readObject();
                System.out.println("주문들어온 음식: " + order.getFood() + "\n수량: " + order.getAmount());

                // 주문 정보 갱신
                unit.setAmount(unit.getAmount() - order.getAmount());

                // 현재 재고 출력
                System.out.println("잔여 수량: " + unit.getFood() + "\n수량: " + unit.getAmount());

                objectOutputStream.reset();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
