package socketServer;

import protocol.Protocol;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.regex.Pattern;

import static java.lang.System.in;
import static java.lang.System.out;

public class Receiver extends Thread implements Runnable {
    OutputStream outputStream = null;
    InputStream inputStream = null;
    Socket socket = null;
    Menu menu = null;
    Client client = null;
    String id;
    int point;
    public Receiver(Socket socket, Menu menu, Client client) {
        this.socket = socket;
        this.menu = menu;
        this.client = client;
    }

    public void actionMain(Protocol protocol) throws IOException {
        id = protocol.getId();
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }
    public void actionLoginRes(Protocol protocol) throws IOException {
        Pattern pattern = Pattern.compile("[ !@#$%^&*(),.?\":{}|<>]");
        id = protocol.getId();
        if (id.length() > 15) {
            protocol = new Protocol(Protocol.PT_LOGIN_FAILED);
            protocol.setFailedMsg("[관리자] 15글자 이하로 설정해야 합니다.");
        }
        if (pattern.matcher(id).find()) {
            protocol = new Protocol(Protocol.PT_LOGIN_FAILED);
            protocol.setFailedMsg("[관리자] 공백 및 특수문자는 사용 불가능합니다.");
        } else {
            if (client.checkId(id)) {
                protocol = new Protocol(Protocol.PT_LOGIN_FAILED);
                protocol.setLoginFailedMsg("[관리자] 이미 존재하는 아이디입니다.");
            } else {
                client.addClient(id);
                protocol = new Protocol(Protocol.PT_MAIN);
                protocol.setId(id);
            }
        }
        outputStream.write(protocol.getPacket());
    }

    public void actionStockReq(Protocol protocol) throws IOException {
        System.out.println("[메뉴판 조회 요청]");
        id = protocol.getId();
        protocol = new Protocol(Protocol.PT_STOCK_RES);
        protocol.setId(id);
        protocol.setMenuName(menu.getFood().toString());
        protocol.setMenuPrice(menu.getPrice().toString());
        protocol.setMenuAmount(menu.getAmount().toString());
        outputStream.write(protocol.getPacket());
    }

    public void actionOrder(Protocol protocol) throws IOException {
        System.out.println("[주문 요청]");
        id = protocol.getId();
        int orderFoodIdx = Integer.parseInt(protocol.getOrderFood()) - 1;
        int orderAmount = Integer.parseInt(protocol.getOrderAmount());
        int orderTotalPrice = Integer.parseInt(protocol.getOrderPrice());
        int clientPoint = client.getPoint(id);
        System.out.println(clientPoint);
        List<Integer> tmpAmountList = menu.getAmount();
        // 재고 부족 시
        if (orderAmount > tmpAmountList.get(orderFoodIdx)) {
            protocol = new Protocol(Protocol.PT_ORDER_FAILED);
            protocol.setId(id);
            protocol.setFailedMsg("[관리자] 재고가 부족합니다.");
        }
        // 잔액 부족
        else if (orderTotalPrice > clientPoint) {
            protocol = new Protocol(Protocol.PT_ORDER_FAILED);
            protocol.setId(id);
            protocol.setFailedMsg("[관리자] 잔여 포인트가 부족합니다.");
        } else {
            tmpAmountList.set(orderFoodIdx, tmpAmountList.get(orderFoodIdx) - orderAmount);
            client.subPoint(id, orderTotalPrice);
            clientPoint = client.getPoint(id);
            menu.setAmount(tmpAmountList);
            protocol = new Protocol(Protocol.PT_ORDER_SUCCESS);
            protocol.setId(id);
            protocol.setSuccessMsg("[관리자] " + id + "님의 잔여 포인트: " + client.getPoint(id) + "point");
        }
        outputStream.write(protocol.getPacket());
    }

    public void actionServiceReq(Protocol protocol) throws IOException {
        System.out.println("[서비스 요청]");
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
    }

    public void actionPointReq(Protocol protocol) throws IOException {
        System.out.println("[포인트 충전 요청]");
        id = protocol.getId();
        int pointReq = Integer.parseInt(protocol.getClientPoint());
        point = client.getPoint(id);
        client.setPoint(id, pointReq);
        protocol = new Protocol(Protocol.PT_POINT_RES);
        protocol.setId(id);
        protocol.setPointMsg(pointReq + "point가 충전되었습니다.");
        outputStream.write(protocol.getPacket());
    }

    public void actionPointLookupReq(Protocol protocol) throws IOException {
        System.out.println("[포인트 조회 요청]");
        id = protocol.getId();
        point = client.getPoint(id);
        protocol = new Protocol(Protocol.PT_LOOKUP_RES);
        protocol.setId(id);
        protocol.setPointMsg("[관리자] " + id + "님의 현재 포인트는 " + point + "point 입니다.");
        outputStream.write(protocol.getPacket());
    }

    public void actionExitReq(Protocol protocol) throws IOException {
        System.out.println("[종료 요청]");
        id = protocol.getId();
        client.removeClient(id);
        protocol = new Protocol(Protocol.PT_EXIT_RES);
        outputStream.write(protocol.getPacket());
    }

    @Override
    public void run() {
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            Protocol protocol = new Protocol(Protocol.PT_LOGIN_REQ);
            outputStream.write(protocol.getPacket());
            while (true) {
                protocol = new Protocol();
                byte[] buf = protocol.getPacket();
                inputStream.read(buf);
                int packetType = buf[0];
                protocol.setPacket(packetType, buf);
                switch (packetType) {
                    case Protocol.PT_EXIT_REQ:
                        actionExitReq(protocol);
                        break;
                    case Protocol.PT_MAIN:
                        actionMain(protocol);
                        break;
                    case Protocol.PT_LOGIN_RES:
                        actionLoginRes(protocol);
                        break;
                    case Protocol.PT_STOCK_REQ:
                        actionStockReq(protocol);
                        break;
                    case Protocol.PT_ORDER:
                        actionOrder(protocol);
                        break;
                    case Protocol.PT_SERVICE_REQ:
                        actionServiceReq(protocol);
                        break;
                    case Protocol.PT_POINT_REQ:
                        actionPointReq(protocol);
                        break;
                    case Protocol.PT_POINT_LOOKUP_REQ:
                        actionPointLookupReq(protocol);
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
