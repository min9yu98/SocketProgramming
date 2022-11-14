package socketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private final static int SERVER_PORT = 1234;
    Menu menu = null;
    public static void main(String args[]){
        try {
            //서버 소켓 생성
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            Menu menu = Menu.getInstance();
            System.out.println("Server Start");

            // 판매할 메뉴 정보 입력
            InputStream in = System.in;
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            ArrayList<String> menuNameList = new ArrayList<>();
            ArrayList<Integer> menuPriceList = new ArrayList<>();
            ArrayList<Integer> menuAmountList = new ArrayList<>();
            try {
                System.out.println("추가할 메뉴의 수를 입력하세요: ");
                String menuCntString = br.readLine();
                int menuCnt = Integer.parseInt(menuCntString);
                for (int i = 0; i < menuCnt; i++) {
                    System.out.print(i + 1 + "번 메뉴명: ");
                    String menuNmae = br.readLine();
                    menuNameList.add(menuNmae);
                }
                System.out.println("각 메뉴의 가격을 입력하세요: ");
                for (int i = 0; i < menuCnt; i++) {
                    System.out.print(i + 1 + "번 메뉴의 가격: ");
                    String menuPrice = br.readLine();
                    menuPriceList.add(Integer.parseInt(menuPrice));
                };
                System.out.println("각 메뉴의 수량을 입력하세요: ");
                for (int i = 0; i < menuCnt; i++) {
                    System.out.print(i + 1 + "번 메뉴의 수량을 입력하세요: ");
                    String menuAmount = br.readLine();
                    menuAmountList.add(Integer.parseInt(menuAmount));
                }
                menu = new Menu();
                menu.setFood(menuNameList);
                menu.setPrice(menuPriceList);
                menu.setAmount(menuAmountList);
                System.out.println("메뉴 정보 저장 완료!\n\n\n\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            int count = 0;
            Thread[] thread = new Thread[10];
            while (true) {
                Socket socket = serverSocket.accept();
                thread[count] = new Thread(new Receiver(socket, menu));
                thread[count].start();
                count++;
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
