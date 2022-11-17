package socketServer;

import protocol.Protocol;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import static java.lang.System.in;
import static java.lang.System.out;

public class Receiver extends Thread implements Runnable {
    Socket socket = null;
    Menu menu = null;
    String id;
    int point;
    HashMap<String, Integer> clientList = new HashMap<String, Integer>();
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
                        id = protocol.getId();
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_LOGIN_RES:
                        id = protocol.getId();
                        clientList.put(id, 0);
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_STOCK_REQ:
                        System.out.println("메뉴판 조회 요청 들어옴");
                        id = protocol.getId();
                        protocol = new Protocol(Protocol.PT_STOCK_RES);
                        protocol.setId(id);
                        protocol.setMenuName(menu.getFood().toString());
                        protocol.setMenuPrice(menu.getPrice().toString());
                        protocol.setMenuAmount(menu.getAmount().toString());
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_ORDER:
                        System.out.println("주문 요청 들어옴");
                        id = protocol.getId();
                        int orderFoodIdx = Integer.parseInt(protocol.getOrderFood()) - 1;  // 주문 메뉴의 인덱스
                        int orderAmount = Integer.parseInt(protocol.getOrderAmount());
                        int orderTotalPrice = Integer.parseInt(protocol.getOrderPrice());
                        int clientPoint = clientList.get(id);
                        System.out.println(clientPoint);
                        List<Integer> tmpAmountList = menu.getAmount();
                        // 재고 부족 시
                        if (orderAmount > tmpAmountList.get(orderFoodIdx)) {
                            protocol = new Protocol(Protocol.PT_SHORTAGE_STOCK);
                            protocol.setId(id);
                        }
                        // 잔액 부족
                        else if (orderTotalPrice > clientPoint) {
                            protocol = new Protocol(Protocol.PT_SHORTAGE_POINT);
                            protocol.setId(id);
                        } else {
                            tmpAmountList.set(orderFoodIdx, tmpAmountList.get(orderFoodIdx) - orderAmount);
                            clientList.replace(id, clientPoint - orderTotalPrice);
                            clientPoint = clientList.get(id);
                            menu.setAmount(tmpAmountList);
                            protocol = new Protocol(Protocol.PT_ORDER_SUCCESS);
                            protocol.setId(id);
                            protocol.setSuccessMsg("[관리자] " + id + "님의 잔여 포인트: " + clientList.get(id) + "point");
                        }
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_SERVICE_REQ:
                        System.out.println("서비스 요청 들어옴");
                        id = protocol.getId();
                        int serviceType = Integer.parseInt(protocol.getServiceType());
                        protocol = new Protocol(Protocol.PT_SERVICE_RES);
                        protocol.setId(id);
                        if(serviceType == 1){
                            // 휴지 없음
                            protocol.setServiceMsg("[관리자] " + id + "님, 휴지 채워 드렸습니다!");
                        } else if (serviceType == 2) {
                            // 물컵
                            protocol.setServiceMsg("[관리자] " + id + "님, 물컵 채워 드렸습니다!");
                        }
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_POINT_REQ:
                        System.out.println("포인트 충전 요청 들어옴");
                        id = protocol.getId();
                        int pointReq = Integer.parseInt(protocol.getClientPoint());
                        point = clientList.get(id);
                        clientList.put(id, point + pointReq);
                        System.out.println(clientList.get(id));
                        protocol = new Protocol(Protocol.PT_POINT_RES);
                        protocol.setId(id);
                        protocol.setPointMsg(pointReq + "point가 충전되었습니다.");
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_POINT_LOOKUP_REQ:
                        System.out.println("포인트 조회 요청 들어옴");
                        id = protocol.getId();
                        point = clientList.get(id);
                        protocol = new Protocol(Protocol.PT_LOOKUP_RES);
                        protocol.setId(id);
                        protocol.setPointMsg("[관리자] " + id + "님의 현재 포인트는 " + point + "point 입니다.");
                        outputStream.write(protocol.getPacket());
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
