package socketClient;

import protocol.Protocol;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Receiver extends Thread implements Runnable {
    Socket socket = null;
    String id = null;
    String main = null;
    public Receiver(Socket socket) {
        this.socket = socket;
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
                    break;
                }
                switch (packetType) {
                    case Protocol.PT_UNDEFINED:
                        System.out.println("비정상적인 유저입니다.");
                        socket.close();
                        break;
                    case Protocol.PT_LOGIN_RES:
                        String id = protocol.getId();
                        System.out.println(id + " " + "환영합니다! 메뉴를 골라주세요");
                        protocol = new Protocol(Protocol.PT_STOCK_REQ);
                        protocol.setId(id);
                        protocol.setClientType("1");
                        outputStream.write(protocol.getPacket());
                        break;
                    case Protocol.PT_LOGIN_REQ:
                        System.out.println("로그인을 해주세요");
                        System.out.print("ID를 입력하세요: ");
                        id = br.readLine();
                        protocol = new Protocol(Protocol.PT_LOGIN_RES);
                        protocol.setId(id);
                        protocol.setClientType("1");
                        outputStream.write(protocol.getPacket());
                        break;

                    case Protocol.PT_MAIN:
                        id = protocol.getId();
                        System.out.println("1. 주문하기");
                        System.out.println("2. 요청 보내기");
                        System.out.println("3. 서비스 종료");
                        main = br.readLine();

                        if (main.equals("1")) {
                            protocol = new Protocol(Protocol.PT_STOCK_REQ);
                            protocol.setId(id);
                            protocol.setClientType("1");
                            outputStream.write(protocol.getPacket());
                        } else if (main.equals("2")) {
                            break;
                        } else if (main.equals("3")) {
                            System.out.println("서비스를 종료합니다.");
                            break;
                        } else {
                            System.out.println("잘못된 입력입니다.");
                            break;
                        }
                        break;
                    case Protocol.PT_STOCK_RES:
                        id = protocol.getId();
                        System.out.println("[" + protocol.getId() + "님 환영합니다! 메뉴를 골라주세요!]");
                        System.out.println("<오늘의 메뉴>");
                        System.out.println(protocol.getMenuName());
                        System.out.println(protocol.getMenuAmount());
                        System.out.println(protocol.getMenuPrice());
                        System.out.print("주문할 메뉴의 번호를 입력하세요: ");
                        String menuName = br.readLine();
                        System.out.print("수량을 입력하세요: ");
                        String menuAmount = br.readLine();
                        protocol = new Protocol(Protocol.PT_ORDER);
                        protocol.setId(id);
                        protocol.setClientType("1");
                        protocol.setOrderFood(menuName);
                        protocol.setOrderAmount(menuAmount);
                        // 가격 정보 얻어오기와 고객의 잔액 정보 추가하기
                        protocol.setOrderPrice("1234");
                        protocol.setClientBalance("9999999");
                        outputStream.write(protocol.getPacket());
                        break;
//                    case Protocol.PT_ORDER_INVALID_MENU:
//                        // 유효하지 않은 주문 번호
//                        System.out.println("잘못된 정보입니다.");
//
//                        break;
//                    case Protocol.PT_ORDER_INVALID_AMOUNT:
//                        // 유효하지 않은 수량
//                        System.out.println("수량이 맞지 않습니다.");
//                        break;
//                    case Protocol.PT_ORDER_INVALID_BALANCE:
//                        // 잔액부족
//                        System.out.println("잔액이 부족합니다.");
//                    case Protocol.PT_ORDER_VALID:
//                        System.out.println("정상 처리 되었습니다.");
//                        protocol = new Protocol(Protocol.PT_SUCCESS);
//                        protocol.setId(id);
//                        protocol.setClientType("1");
//                        break;

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
