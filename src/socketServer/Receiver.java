package socketServer;

import protocol.Protocol;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import static java.lang.System.in;

public class Receiver extends Thread implements Runnable {
    Socket socket = null;
    Menu menu = null;
    //HashMap<String, Integer> clinetMap = new HashMap<String, Integer>();
    String clientType;
    String id;
    List<String> clientList = new ArrayList<>();
    public Receiver(Socket socket, Menu menu) {
        this.socket = socket;
        this.menu = menu;
    }

    @Override
    public void run() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // 처음 접속하는 경우 로그인 요청
            Protocol protocol = new Protocol(Protocol.PT_LOGIN_REQ);
            outputStream.write(protocol.getPacket());

            while (true) {
                protocol = new Protocol();
                byte[] buf = protocol.getPacket();
                inputStream.read(buf);
                int packetType = buf[0];
                protocol.setPacket(packetType, buf);
                switch (packetType) {
                    case Protocol.PT_MAIN:
                        clientType = protocol.getClientType();
                        id = protocol.getId();
                        // 비정상 사용자라면 소켓 중지
                        if (clientType.equals("0")) {
                            protocol = new Protocol(Protocol.PT_UNDEFINED);
                            outputStream.write(protocol.getPacket());
                            break;
                        }
                        clientList.add(id);
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        protocol.setClientType("1");
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_LOGIN_RES:
                        String id = protocol.getId();
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        protocol.setClientType("1");
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_STOCK_REQ:
                        System.out.println("메뉴판 조회 요청 들어옴");
                        clientType = protocol.getClientType();
                        id = protocol.getId();
                        boolean flag = false;
                        // 비정상 사용자라면 소켓 중지
                        if (clientType.equals("0"))
                            flag = false;
                        for(String clientId : clientList){
                            if(clientId.equals(id)){
                                flag = true;
                                break;
                            }
                        }
                        // 정상적인 경우
                        if (flag) {
                            protocol = new Protocol(Protocol.PT_STOCK_RES);
                            protocol.setId(id);
                            protocol.setClientType("1");
                            protocol.setMenuName(menu.getFood().toString());
                            protocol.setMenuPrice(menu.getPrice().toString());
                            protocol.setMenuAmount(menu.getAmount().toString());
                        } else {
                            protocol = new Protocol(Protocol.PT_UNDEFINED);
                        }
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
