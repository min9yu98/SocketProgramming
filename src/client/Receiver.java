package client;

import java.io.*;
import java.net.Socket;

public class Receiver extends Thread implements Runnable {
    Socket socket = null;

    public Receiver(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        // 콘솔창에 입력하는 값을 버퍼에 저장
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            // 서버로 보내는 스트림
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);  // 보조 스트림이라 기반 스트림이 필요
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);  // 보조 스트림이라 기반 스트림이 필요
            Unit unit = new Unit();
            while (true) {
                // 서버로부터 현재 메뉴 받아서 콘솔에 출력
                unit = (Unit) objectInputStream.readObject();
                System.out.println("메뉴: " + unit.getFood() + "\n수량: " + unit.getAmount());

                // 주만할 음식과 수량 입력
                System.out.println("주문할 음식: ");
                String food = bufferedReader.readLine();
                System.out.println("수량: ");
                String amount = bufferedReader.readLine();

                // client.Unit 객체에 저장
                Order order = new Order();
                order.setFood(food);
                order.setAmount(Integer.parseInt(amount));

                // 주문 전송
                objectOutputStream.writeObject(order);
                objectOutputStream.reset();

                // 콘솔창 클리어
                System.out.println("\n\n\n\n\n\n\n\n\n\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
