package socketClient;

import protocol.Protocol;

import java.io.*;
import java.net.Socket;

public class Receiver extends Thread implements Runnable {
    Socket socket = null;
    String id = null;
    String main = null;
    private String request;
    private int point = 0;
    private String menuName, menuAmount;
    OutputStream outputStream;
    InputStream inputStream;
    InputStream in;
    InputStreamReader reader;
    BufferedReader br;
    byte[] buf;
    Protocol protocol = new Protocol();
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

    public void clear() {
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    }

    public void actionLoginRes(Protocol protocol) throws IOException {
        String id = protocol.getId();
        System.out.println(id + " " + "환영합니다! 메뉴를 골라주세요.");
        protocol = new Protocol(Protocol.PT_STOCK_REQ);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }

    public void actionLogReq(Protocol protocol) throws IOException {
        System.out.println("로그인을 해주세요"); // 로그인
        System.out.print("ID를 입력하세요: ");
        String id = br.readLine();
        protocol = new Protocol(Protocol.PT_LOGIN_RES);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
        clear();
    }

    public void actionLoginFailed(Protocol protocol) throws IOException {
        System.out.println(protocol.getLoginFailedMsg());
        System.out.print("ID를 입력하세요: ");
        id = br.readLine();
        protocol = new Protocol(Protocol.PT_LOGIN_RES);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
        clear();
    }

    public void actionMain(Protocol protocol) throws IOException {
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
                    System.out.println("3. 되돌아가기");
                    System.out.print("서비스 요청 사항 선택: ");
                    request = br.readLine();
                    if (Integer.parseInt(request) < 1 || Integer.parseInt(request) > 3) {
                        System.out.println("잘못된 서비스 요청 사항입니다.");
                        continue;
                    }
                    break;
                }
                if (Integer.parseInt(request) == 3) {
                    protocol = new Protocol(Protocol.PT_MAIN);
                } else {
                    protocol = new Protocol(Protocol.PT_SERVICE_REQ);
                    protocol.setServiceType(request);
                }
                protocol.setId(id);
                outputStream.write(protocol.getPacket());
                break;
            } else if (main.equals("3")) { // 포인트 충전
                String inputPoint;
                while (true) {
                    System.out.print("충전할 포인트을 입력해주세요(되돌아가기는 q를 눌러주세요): ");
                    inputPoint = br.readLine();
                    if (inputPoint.equals("q")) {
                        break;
                    }
                    if (Integer.parseInt(inputPoint) > 100000) {
                        System.out.println("최대 10만원까지 충전 가능합니다.");
                        continue;
                    }
                    break;
                }
                if (inputPoint.equals("q")) {
                    protocol = new Protocol(Protocol.PT_MAIN);
                } else {
                    protocol = new Protocol(Protocol.PT_POINT_REQ);
                    protocol.setClientPoint(inputPoint);
                }
                protocol.setId(id);
                outputStream.write(protocol.getPacket());
                break;
            } else if (main.equals("4")) { // 잔여 포인트 조회
                protocol = new Protocol(Protocol.PT_POINT_LOOKUP_REQ);
                protocol.setId(id);
                outputStream.write(protocol.getPacket());
                break;
            } else if (main.equals("5")) {
                protocol = new Protocol(Protocol.PT_EXIT_REQ);
                protocol.setId(id);
                outputStream.write(protocol.getPacket());
                break;
            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }
        clear();
    }

    public void actionStockRes(Protocol protocol) throws IOException {
        id = protocol.getId();
        System.out.println("[" + protocol.getId() + "님 환영합니다! 메뉴를 골라주세요!]");
        System.out.println("(되돌아가기는 q를 눌러주세요)");
        System.out.println("<오늘의 메뉴>");
        String[] menuList = fromString(protocol.getMenuName());
        String[] amountList = fromString(protocol.getMenuAmount());
        String[] priceList = fromString(protocol.getMenuPrice());
        for (int i = 0; i < menuList.length; i++){
            System.out.println((i + 1) + ". 메뉴:" + menuList[i] + " 남은 수량: " + amountList[i] + " 가격: " + priceList[i]);
        }
        // 메뉴 번호 입력 - 잘못입력시 while문 제대로 입력할 때까지
        while (true){
            System.out.print("주문할 메뉴의 번호를 입력하세요: ");
            menuName = br.readLine();
            // 메뉴번호 확인
            if (menuName.equals("q")) {
                protocol = new Protocol(Protocol.PT_MAIN);
                break;
            }
            if (0 >= Integer.parseInt(menuName) || menuList.length < Integer.parseInt(menuName)) {
                System.out.println("잘못된 주문 번호입니다.");
                continue;
            }
            while (true){
                System.out.println("메뉴 수정은 'm'을, 주문 종료는 'q'를 입력해주세요.");
                System.out.print("수량을 입력하세요 (100개 이하로 입력해주세요): ");
                menuAmount = br.readLine();
                if (menuAmount.equals("m") || menuAmount.equals("q")) {
                    break;
                }
                // 수량 확인
                if (100 < Integer.parseInt(menuAmount) || 0 >= Integer.parseInt(menuAmount)){
                    System.out.println("잘못된 수량 입니다.");
                    continue;
                }
                break;
            }
            if (menuAmount.equals("m")) {
                continue;
            } else if (menuAmount.equals("q")) {
                protocol = new Protocol(Protocol.PT_MAIN);
                break;
            }
            String check;
            while (true) {
                System.out.print("주문을 완료하시겠습니까?(y/n) ");
                check = br.readLine();
                if (check.equals("y") || check.equals("n")) {
                    break;
                } else {
                    System.out.println("잘못된 입력입니다. 다시 입력해주세요.");
                }
            }
            if (check.equals("y")) {
                protocol = new Protocol(Protocol.PT_ORDER);
                protocol.setOrderFood(menuName);
                protocol.setOrderAmount(menuAmount);
                int total_order = Integer.parseInt(priceList[Integer.parseInt(menuName) - 1]) * Integer.parseInt(menuAmount);
                protocol.setOrderPrice(String.valueOf(total_order));
            } else {
                protocol = new Protocol(Protocol.PT_MAIN);
            }
            break;
        }
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }

    public void actionPointRes(Protocol protocol) throws IOException {
        System.out.println(protocol.getPointMsg());
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
        System.out.println();
        System.out.println();
    }

    public void actionServiceRes(Protocol protocol) throws IOException {
        System.out.println(protocol.getServiceMsg());
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
        System.out.println();
        System.out.println();
    }

    public void actionOrderFailed(Protocol protocol) throws IOException {
        System.out.println(protocol.getFailedMsg());
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
        System.out.println();
        System.out.println();
    }

    public void actionOrderSuccess(Protocol protocol) throws IOException, InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread.sleep(300);
            System.out.println(".");
        }
        Thread.sleep(300);
        System.out.println(protocol.getSuccessMsg());
        // 성공
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
        System.out.println();
        System.out.println();
    }

    public void run() {
        try {
            // 서버로 보내는 스트림
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            buf = protocol.getPacket();
            // 콘솔창에 입력받기 위한 스트림
            in = System.in;
            reader = new InputStreamReader(in);
            br = new BufferedReader(reader);
            while (true) {
                inputStream.read(buf);  // 서버에서 받은 바이트를 buf에 저장
                int packetType = buf[0];
                protocol.setPacket(packetType, buf);  // buf의 값을 protocol에 복사
                if (packetType == Protocol.PT_EXIT) { // 서비스 종료
                    System.out.println("클라이언트 종료");
                    socket.close();
                    break;
                }
                switch (packetType) {
                    case Protocol.PT_UNDEFINED: // 비정상적인 유저 처리
                        clear();
                        System.out.println("비정상적인 유저입니다.");
                        socket.close();
                        break;
                    case Protocol.PT_LOGIN_FAILED:
                        actionLoginFailed(protocol);
                        break;
                    case Protocol.PT_LOGIN_RES: // 로그인 후 인증 완료
                        actionLoginRes(protocol);
                        break;
                    case Protocol.PT_LOGIN_REQ:
                        actionLogReq(protocol);
                        break;
                    case Protocol.PT_MAIN: // 서비스 목록
                        actionMain(protocol);
                        break;
                    case Protocol.PT_STOCK_RES: // 주문 입력
                        actionStockRes(protocol);
                        break;
                    case Protocol.PT_LOOKUP_RES: // 포인트 조회
                    case Protocol.PT_POINT_RES: // 포인트 조회
                        actionPointRes(protocol);
                        break;
                    case Protocol.PT_SERVICE_RES: // 요청 사항에 대한 관리자 메시지 처리
                        actionServiceRes(protocol);
                        break;
                    case Protocol.PT_ORDER_FAILED: // 주문 실패
                        actionOrderFailed(protocol);
                        break;
                    case Protocol.PT_ORDER_SUCCESS: // 주문 성공
                        actionOrderSuccess(protocol);
                        break;
                    case Protocol.PT_EXIT_RES:
                        socket.close();
                        break;
                }
            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("서비스가 종료되었습니다.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
