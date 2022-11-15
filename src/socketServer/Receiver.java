package socketServer;

import protocol.Protocol;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static java.lang.System.in;

public class Receiver extends Thread implements Runnable {
    Socket socket = null;
    Menu menu = null;
    public Receiver(Socket socket, Menu menu) {
        this.socket = socket;
        this.menu = menu;
    }

    @Override
    public void run() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // 처음 접속하는 경우 호그인 요청
            Protocol protocol = new Protocol(Protocol.PT_LOGIN_REQ);
            outputStream.write(protocol.getPacket());

            while (true) {
                protocol = new Protocol();
                byte[] buf = protocol.getPacket();
                inputStream.read(buf);

                int packetType = buf[0];
                protocol.setPacket(packetType, buf);
                switch (packetType) {
                    case Protocol.PT_UNDEFINED:
                        System.out.println("비정상적인 유저입니다.");
                        socket.close();
                        break;
                    case Protocol.PT_LOGIN_RES:
                        String id = protocol.getId();
                        String pwd = protocol.getPWD();
                        System.out.println(id + " " + pwd + "환영합니다! 메뉴를 골라주세요");
                        protocol = new Protocol(Protocol.PT_STOCK);
                        protocol.setId(id);
                        protocol.setMenuName(menu.getFood().toString());
                        protocol.setMenuPrice(menu.getPrice().toString());
                        protocol.setMenuAmount(menu.getAmount().toString());
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_STOCK:
                        System.out.println("메뉴판 조회 요청 들어옴");
                        protocol = new Protocol(Protocol.PT_STOCK);
                        // 요청한 사용자의 아이디를 덛는 함수 작성하기
                        protocol.setId("test");
                        protocol.setMenuName(menu.getFood().toString());
                        protocol.setMenuPrice(menu.getPrice().toString());
                        protocol.setMenuAmount(menu.getAmount().toString());
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_ORDER:
                        System.out.println("주문 들어옴");
                        System.out.println("주문 음식 :" + protocol.getOrderFood());
                        System.out.println("주문 수량: " + protocol.getOrderAmount());
                        System.out.println("총 금액: " + protocol.getOrderPrice());
                        System.out.println("고객의 잔액: " + protocol.getClientBalance());
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
