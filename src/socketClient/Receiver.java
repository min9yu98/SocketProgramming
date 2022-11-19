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

    public void consoleClear() {
        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    }

    public void printLoading() throws InterruptedException {
        System.out.println();
        for (int i = 0; i < 3; i++) {
            System.out.println(". ");
            Thread.sleep(600);
        }
        System.out.println("\n[NOTICE] 주문이 접수되었습니다!");
    }

    public void printHaksik() throws InterruptedException {
        System.out.println("\n\n");
        Thread.sleep(200);
        System.out.println("   __    _   _             _              _    _   __   __ ");
        Thread.sleep(200);
        System.out.println("  / /   | | | |           | |            (_)  | | / /   \\ \\");
        Thread.sleep(200);
        System.out.println(" / /    | |_| |    __ _   | | __   ___    _   | |/ /     \\ \\");
        Thread.sleep(200);
        System.out.println("< <     |  _  |   / _` |  | |/ /  / __|  | |  |    \\      > >");
        Thread.sleep(200);
        System.out.println(" \\ \\    | | | |  | (_| |  |   <   \\__ \\  | |  | |\\  \\    / /");
        Thread.sleep(200);
        System.out.println("  \\_\\   \\_| |_/   \\__,_|  |_|\\_\\  |___/  |_|  \\_| \\_/   /_/     made by GOAT\n\n");
        Thread.sleep(500);
        System.out.print("[INPUT ID]\n> ");
    }

    public void printGoodbye() {
        System.out.println("\n" +
                " _____                    _ ______              \n" +
                "|  __ \\                  | || ___ \\             \n" +
                "| |  \\/  ___    ___    __| || |_/ / _   _   ___ \n" +
                "| | __  / _ \\  / _ \\  / _` || ___ \\| | | | / _ \\\n" +
                "| |_\\ \\| (_) || (_) || (_| || |_/ /| |_| ||  __/\n" +
                " \\____/ \\___/  \\___/  \\__,_|\\____/  \\__, | \\___|\n" +
                "                                     __/ |      \n" +
                "                                    |___/       \n");
    }

    public void pause() {
        try {
            br.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void actionLoginRes(Protocol protocol) throws IOException {
        String id = protocol.getId();
        System.out.println(id + " " + "환영합니다! 메뉴를 골라주세요.");
        protocol = new Protocol(Protocol.PT_STOCK_REQ);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }

    public void actionLogReq(Protocol protocol) throws IOException, InterruptedException {
        printHaksik();
        String id = br.readLine();
        protocol = new Protocol(Protocol.PT_LOGIN_RES);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
        consoleClear();
    }

    public void actionLoginFailed(Protocol protocol) throws IOException {
        System.out.println(protocol.getLoginFailedMsg());
        System.out.print("INPUT ID\n> ");
        id = br.readLine();
        protocol = new Protocol(Protocol.PT_LOGIN_RES);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
        consoleClear();
    }

    public void actionMain(Protocol protocol) throws IOException {
        id = protocol.getId();
        while (true){
            consoleClear();
            System.out.println("[  MAIN  ]");
            System.out.println("[1] 주문");
            System.out.println("[2] 서비스 요청 전송");
            System.out.println("[3] 포인트 충전");
            System.out.println("[4] 포인트 잔액 조회");
            System.out.println("[5] 서비스 종료");
            System.out.print("> ");
            main = br.readLine();
            if (main.equals("1")) {
                protocol = new Protocol(Protocol.PT_STOCK_REQ);
                protocol.setId(id);
                outputStream.write(protocol.getPacket());
                break;
            } else if (main.equals("2")) {
                while (true){
                    consoleClear();
                    System.out.println("[  SERVICE  ]");
                    System.out.println("[1] 휴지가 부족해요!");
                    System.out.println("[2] 물컵이 부족해요!");
                    System.out.println("[3] 뒤로가기");
                    System.out.print("> ");
                    request = br.readLine();
                    if (Integer.parseInt(request) < 1 || Integer.parseInt(request) > 3) {
                        System.out.print("[ERROR] 잘못된 서비스 요청입니다.");
                        pause();
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
                    consoleClear();
                    System.out.println("[  POINT  ]");
                    System.out.print("충전할 포인트을 입력해주세요(뒤로가기: q)\n> ");
                    inputPoint = br.readLine();
                    if (inputPoint.equals("q")) {
                        break;
                    }
                    if (Integer.parseInt(inputPoint) > 100000) {
                        System.out.print("[ERROR] 최대 10만원까지 충전 가능합니다.");
                        pause();
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
            } else if (main.equals("4")) {
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
                System.out.print("[ERROR] 잘못된 입력입니다.");
                pause();
            }
        }
    }

    public void actionStockRes(Protocol protocol) throws IOException, InterruptedException {
        id = protocol.getId();
        String[] menuList = fromString(protocol.getMenuName());
        String[] amountList = fromString(protocol.getMenuAmount());
        String[] priceList = fromString(protocol.getMenuPrice());
        while (true){
            consoleClear();
            System.out.println("[  MENU  ]");
            for (int i = 0; i < menuList.length; i++){
                System.out.print("[" + (i + 1) + "] ");
                if (amountList[i].equals("0")) {
                    System.out.println("SOLD-OUT");
                } else {
                    System.out.println("메뉴명: " + menuList[i] + " | 남은 수량: " + amountList[i] + " | 가격: " + priceList[i]);
                }
            }
            System.out.print("주문할 메뉴의 번호를 입력하세요(주문 취소: q)\n> ");
            menuName = br.readLine();
            if (menuName.equals("q")) {
                protocol = new Protocol(Protocol.PT_MAIN);
                break;
            } else if (0 >= Integer.parseInt(menuName) || menuList.length < Integer.parseInt(menuName)) {
                System.out.print("[ERROR] 잘못된 주문 번호입니다.");
                pause();
                continue;
            } else if (amountList[Integer.parseInt(menuName) - 1].equals("0")) {
                System.out.print("[NOTICE] 해당 메뉴는 주문이 불가능합니다.");
                pause();
                continue;
            }
            while (true){
                System.out.print("수량을 입력하세요(메뉴 수정: m, 주문 취소: q)\n> ");
                menuAmount = br.readLine();
                if (menuAmount.equals("m") || menuAmount.equals("q")) {
                    break;
                }
                // 수량 확인
                if (100 < Integer.parseInt(menuAmount) || 0 >= Integer.parseInt(menuAmount)){
                    System.out.print("[ERROR] 최소 1개, 최대 100개 까지 주문 가능합니다.");
                    pause();
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
            while (true) {
                System.out.print("주문을 완료하시겠습니까?(y/n)\n> ");
                String check = br.readLine();
                if (check.equals("y")) {
                    protocol = new Protocol(Protocol.PT_ORDER);
                    protocol.setOrderFood(menuName);
                    protocol.setOrderAmount(menuAmount);
                    int total_order = Integer.parseInt(priceList[Integer.parseInt(menuName) - 1]) * Integer.parseInt(menuAmount);
                    protocol.setOrderPrice(String.valueOf(total_order));
                    break;
                } else if (check.equals("n")) {
                    System.out.println("[NOTICE] 주문이 취소되었습니다. 2초 후 메인 화면으로 돌아갑니다.");
                    Thread.sleep(2000);
                    protocol = new Protocol(Protocol.PT_MAIN);
                    break;
                } else {
                    System.out.print("[ERROR] 입력값이 잘못되었습니다.");
                    pause();
                }
            }
            break;
        }
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }

    public void actionPointRes(Protocol protocol) throws IOException {
        System.out.print(protocol.getPointMsg());
        pause();
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }

    public void actionServiceRes(Protocol protocol) throws IOException {
        System.out.print(protocol.getServiceMsg());
        pause();
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }

    public void actionOrderFailed(Protocol protocol) throws IOException {
        System.out.print(protocol.getFailedMsg());
        pause();
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }

    public void actionOrderSuccess(Protocol protocol) throws IOException, InterruptedException {
        printLoading();
        System.out.print(protocol.getSuccessMsg());
        pause();
        protocol = new Protocol(Protocol.PT_MAIN);
        protocol.setId(id);
        outputStream.write(protocol.getPacket());
    }

    public void run() {
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            buf = protocol.getPacket();
            in = System.in;
            reader = new InputStreamReader(in);
            br = new BufferedReader(reader);
            while (true) {
                inputStream.read(buf);
                int packetType = buf[0];
                protocol.setPacket(packetType, buf);
                if (packetType == Protocol.PT_EXIT_RES) {
                    consoleClear();
                    printGoodbye();
                    socket.close();
                    break;
                }
                switch (packetType) {
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
                }
            }
        } catch (Exception e) {
            System.out.println("[WARNIG] ERROR OCCURED");
        }
    }
}
