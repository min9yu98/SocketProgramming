package socketClient;

import protocol.Protocol;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Receiver extends Thread implements Runnable {
    Socket socket = null;
    String id = null;
    String main = null;
    private String request;
    private int point = 0;
    public Receiver(Socket socket) {
        this.socket = socket;
    }

    public String[] fromString(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        String result[] = new String[strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = strings[i];
        }
        return strings;
    }

    public void run() {
        try {
            // 서버로 보내는 스트림
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            Protocol protocol = new Protocol();
            byte[] buf = protocol.getPacket();
            // 콘솔창에 입력받기 위한 스트림
            InputStream in = System.in;
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            while (true) {
                inputStream.read(buf);  // 서버에서 받은 바이트를 buf에 저장
                int packetType = buf[0];
                protocol.setPacket(packetType, buf);  // buf의 값을 protocol에 복사
                if (packetType == Protocol.PT_EXIT) {
                    System.out.println("클라이언트 종료");
                    socket.close();
                    break;
                }
                switch (packetType) {
                    case Protocol.PT_UNDEFINED:
                        System.out.println("비정상적인 유저입니다.");
                        socket.close();
                        break;
                    case Protocol.PT_LOGIN_RES:
                        id = protocol.getId();
                        System.out.println(id + " " + "환영합니다! 메뉴를 골라주세요");
                        protocol = new Protocol(Protocol.PT_STOCK_REQ);
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_LOGIN_REQ:
                        System.out.println("로그인을 해주세요");
                        System.out.print("ID를 입력하세요: ");
                        id = br.readLine();
                        protocol = new Protocol(Protocol.PT_LOGIN_RES);
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_MAIN:
                        id = protocol.getId();
                        while (true){
                            System.out.println("1. 주문");
                            System.out.println("2. 서비스 요청 전송");
                            System.out.println("3. 포인트 충전");
                            System.out.println("4. 포인트 잔액 조회");
                            System.out.println("5. 서비스 종료");
                            main = br.readLine();
                            if (main.equals("1")) {
                                protocol = new Protocol(Protocol.PT_STOCK_REQ);
                                protocol.setId(id);
                                outputStream.write(protocol.getPacket());
                                break;
                            } else if (main.equals("2")) {
                                while (true){
                                    System.out.println("< 서비스 요청 사항 >");
                                    System.out.println("1. 휴지가 부족해요!");
                                    System.out.println("2. 물컵이 부족해요!");
                                    System.out.print("서비스 요청 사항 선택: ");
                                    request = br.readLine();
                                    if (Integer.parseInt(request) < 1 || Integer.parseInt(request) > 2) {
                                        System.out.println("잘못된 서비스 요청 사항입니다.");
                                        continue;
                                    }
                                    break;
                                }
                                protocol = new Protocol(Protocol.PT_SERVICE_REQ);
                                protocol.setId(id);
                                protocol.setServiceType(request);
                                outputStream.write(protocol.getPacket());
                                break;
                            } else if (main.equals("3")) { // 포인트 충전
                                String inputPoint;
                                while (true) {
                                    System.out.print("충전할 포인트을 입력해주세요: ");
                                    inputPoint = br.readLine();
                                    if (Integer.parseInt(inputPoint) > 100000) {
                                        System.out.println("최대 10만원까지 충전 가능합니다.");
                                        continue;
                                    }
                                    break;
                                }
                                protocol = new Protocol(Protocol.PT_POINT_REQ);
                                protocol.setId(id);
                                protocol.setClientPoint(inputPoint);
                                outputStream.write(protocol.getPacket());
                                break;
                            } else if (main.equals("4")) { // 잔여 포인트 조회
                                protocol = new Protocol(Protocol.PT_POINT_LOOKUP_REQ);
                                protocol.setId(id);
                                outputStream.write(protocol.getPacket());
                                break;
                            } else if (main.equals("5")) {
                                socket.close();
                            } else {
                                System.out.println("잘못된 입력입니다.");
                            }
                            System.out.println();
                        }
                        System.out.println();
                        break;

                    case Protocol.PT_STOCK_RES:
                        id = protocol.getId();
                        System.out.println("[" + protocol.getId() + "님 환영합니다! 메뉴를 골라주세요!]");
                        System.out.println("<오늘의 메뉴>");
                        String[] menuList = fromString(protocol.getMenuName());
                        String[] amountList = fromString(protocol.getMenuAmount());
                        String[] priceList = fromString(protocol.getMenuPrice());
                        for (int i = 0; i < menuList.length; i++){
                            System.out.println((i + 1) + ". 메뉴:" + menuList[i] + " 남은 수량: " + amountList[i] + " 가격: " + priceList[i]);
                        }
                        // 메뉴 번호 입력 - 잘못입력시 while문 제대로 입력할 때까지
                        String menuName;
                        while (true){
                            System.out.print("주문할 메뉴의 번호를 입력하세요: ");
                            menuName = br.readLine();
                            // 메뉴번호 확인
                            if (0 >= Integer.parseInt(menuName) || menuList.length < Integer.parseInt(menuName)) {
                                System.out.println("잘못된 주문 번호입니다.");
                                continue;
                            }
                            break;
                        }
                        // 메뉴 수량 입력 - 잘못입력시 while문 제대로 입력할 때까지
                        String menuAmount;
                        while (true){
                            System.out.print("수량을 입력하세요 (100개 이하로 입력해주세요): ");
                            menuAmount = br.readLine();
                            // 수량 확인
                            if (100 < Integer.parseInt(menuAmount) || 0 >= Integer.parseInt(menuAmount)){
                                System.out.println("잘못된 수량 입니다.");
                                continue;
                            }
                            break;
                        }
                        protocol = new Protocol(Protocol.PT_ORDER);
                        protocol.setId(id);
                        protocol.setOrderFood(menuName);
                        protocol.setOrderAmount(menuAmount);
                        int total_order = Integer.parseInt(priceList[Integer.parseInt(menuName) - 1]) * Integer.parseInt(menuAmount);
                        System.out.println(total_order);
                        protocol.setOrderPrice(String.valueOf(total_order));
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_LOOKUP_RES:
                    case Protocol.PT_POINT_RES:
                        System.out.println(protocol.getPointMsg());
                        protocol = new Protocol(Protocol.PT_MAIN); // 찾았다 시발련
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_SERVICE_RES:
                        System.out.println(protocol.getServiceMsg());
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_SHORTAGE_POINT:
                        // 잔액부족
                        System.out.println("잔액이 부족합니다.");
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_SHORTAGE_STOCK:
                        // 수량부족
                        System.out.println("입력 수량이 재고보다 많습니다.");
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_ORDER_SUCCESS:
                        System.out.println(protocol.getSuccessMsg());
                        // 성공
                        protocol = new Protocol(Protocol.PT_MAIN);
                        protocol.setId(id);
                        outputStream.write(protocol.getPacket());
                        break;
                }
            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("서비스가 종료되었습니다.");
        }
    }
}
