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
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        protocol.setClientType("1");
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_LOGIN_RES:
                        id = protocol.getId();
                        clientList.add(id);
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
                        System.out.println("주문 요청 들어옴");
                        clientType = protocol.getClientType();
                        id = protocol.getId();
                        if (clientType.equals("0")) {
                            protocol = new Protocol(Protocol.PT_UNDEFINED);
                        } else {
                            int orderFoodIdx = Integer.parseInt(protocol.getOrderFood()) - 1;  // 주문 메뉴의 인덱스
                            int orderAmount = Integer.parseInt(protocol.getOrderAmount());
                            int orderTotalPrice = Integer.parseInt(protocol.getOrderPrice());
                            int clientBalance = Integer.parseInt(protocol.getClientBalance());
                            List<Integer> tmpAmountList = menu.getAmount();
                            // 재고 부족 시
                            if (orderAmount > tmpAmountList.get(orderFoodIdx)) {
                                protocol = new Protocol(Protocol.PT_SHORTAGE_STOCK);
                                protocol.setId(id);
                                protocol.setClientType("1");
                            }
                            // 잔액 부족
                            else if (orderTotalPrice > clientBalance) {
                                protocol = new Protocol(Protocol.PT_SHORTAGE_BALANCE);
                                protocol.setId(id);
                                protocol.setClientType("1");
                            } else {
                                tmpAmountList.set(orderFoodIdx, tmpAmountList.get(orderFoodIdx) - orderAmount);
                                menu.setAmount(tmpAmountList);
                                protocol = new Protocol(Protocol.PT_ORDER_SUCCESS);
                                protocol.setId(id);
                                protocol.setClientType("1");
                                protocol.setClientBalanceRes(String.valueOf(clientBalance - orderTotalPrice));
                            }
                        }
                        outputStream.write(protocol.getPacket());
                        //System.out.println("고객의 잔액: " + protocol.getClientBalance());
                        break;
                    case Protocol.PT_SERVICE_REQ:
                        System.out.println("서비스 요청 들어옴");
                        clientType = protocol.getClientType();
                        id = protocol.getId();
                        if (clientType.equals("0")) {
                            protocol = new Protocol(Protocol.PT_UNDEFINED);
                        }
                        int serviceType = Integer.parseInt(protocol.getServiceType());
                        protocol = new Protocol(Protocol.PT_SERVICE_RES);
                        protocol.setId(id);
                        protocol.setClientType("1");
                        if(serviceType == 1){
                            // 휴지 없음
                            protocol.setServiceMsg(id + "님, 휴지 채워 드렸습니다!");
                        } else if (serviceType == 2) {
                            // 물컵
                            protocol.setServiceMsg(id + "님, 물컵 채워 드렸습니다");
                        }
                        outputStream.write(protocol.getPacket());
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
