package protocol;

import lombok.Getter;
import lombok.Setter;

import javax.swing.text.DefaultEditorKit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Protocol implements Serializable {
    // 프로토콜 타입에 대한 변수
    public static final int PT_UNDEFINED = -1;   //프로토콜이 지정되어 있지 않을 경우에
    public static final int PT_EXIT = 0;
    public static final int PT_ORDER = 1;
    public static final int PT_STOCK_RES = 2;
    public static final int PT_STOCK_REQ = 3;
    public static final int PT_LOGIN_REQ = 4;
    public static final int PT_LOGIN_RES = 5;
    public static final int PT_MAIN = 6;
    public static final int PT_NORMAL = 7;
    public static final int PT_ABNORMAL = 8;
    public static final int PT_START_SERVER = 9;

    // 프로토콜 종류의 길이
    public static final int LEN_PROTOCOL_TYPE = 1;

    // 초기 사이즈
    public static final int LEN_MAX_SIZE = 1000;

    // 사용자 판별
    public static final int LEN_CLIENT_TYPE = 2;

    // 로그인
    public static final int LEN_LOGIN_ID = 20;
    public static final int LEN_LOGIN_PWD = 20;

    // 주문
    public static final int LEN_ORDER_AMOUNT = 20;
    public static final int LEN_ORDER_FOOD = 20;
    public static final int LEN_ORDER_PRICE = 20;
    public static final int LEN_CLIENT_BALANCE = 20;

    // 재고 현황
    public static final int LEN_STOCK_MENU = 50;
    public static final int LEN_STOCK_PRICE = 50;
    public static final int LEN_STOCK_AMOUNT = 50;

    protected int protocolType;
    private byte[] packet;   //프로토콜과 데이터의 저장공간이 되는 바이트배열

    // 기본 생성자
    public Protocol() {
        this(PT_START_SERVER);
    }

    // 생성자
    public Protocol(int protocolType){
        this.protocolType = protocolType;
        getPacket(protocolType);
    }

    public byte[] getPacket(int protocolType) {
        if(packet == null) {
            switch(protocolType) {
                case PT_START_SERVER:
                    packet = new byte[LEN_MAX_SIZE];
                    break;
                case PT_UNDEFINED:
                case PT_LOGIN_REQ:
                    packet = new byte[LEN_PROTOCOL_TYPE];
                    break;
                case PT_LOGIN_RES:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE];
                    break;
                case PT_STOCK_RES:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_STOCK_MENU + LEN_STOCK_PRICE + LEN_STOCK_AMOUNT];
                    break;
                case PT_ORDER:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_ORDER_FOOD + LEN_ORDER_AMOUNT + LEN_ORDER_PRICE + LEN_CLIENT_BALANCE];
                    break;
                case PT_MAIN:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE];
                    break;
                case PT_STOCK_REQ:
                    packet = new byte[LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE];
            }
        }
        packet[0] = (byte)protocolType;   //packet 바이트배열의 첫번째 방에 프로토콜타입 상수를 셋팅해 놓는다.
        return packet;
    }

//    //로그인후 성공/실패의 결과값을 프로토콜로 부터 추출하여 문자열로 리턴
//    public String getLoginResult(){
//        //String의 다음 생성자를 사용 : String(byte[] bytes, int offset, int length)
//        return new String(packet, LEN_PROTOCOL_TYPE, LEN_LOGIN_RESULT).trim();
//    }
//
//
//    //String ok를 byte[] 로 만들어서 packet의 프로토콜 타입 바로 뒤에 추가한다.
//    public void setLoginResult(String ok){
//        //arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
//        System.arraycopy(ok.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, ok.trim().getBytes().length);
//    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public int getProtocolType() {
        return protocolType;
    }

    public byte[] getPacket(){
        return packet;
    }

    //Default 생성자로 생성한 후 protocol.Protocol 클래스의 packet 데이터를 바꾸기 위한 메서드
    public void setPacket(int pt, byte[] buf){
        packet = null;
        packet = getPacket(pt);
        protocolType = pt;
        System.arraycopy(buf, 0, packet, 0, packet.length);
    }

    // ClientType
    public String getClientType() {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID, LEN_CLIENT_TYPE).trim();
    }

    public void setClientType(String type) {
        System.arraycopy(type.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID, type.trim().getBytes().length);
    }

    // ID
    public String getId() {
        //String(byte[] bytes, int offset, int length)
        return new String(packet, LEN_PROTOCOL_TYPE, LEN_LOGIN_ID).trim();
    }

    public void setId(String id){
        System.arraycopy(id.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE, id.trim().getBytes().length);
    }
//    // Stock 배열의 자료형
//    public int getPTStockDT() {
//        return Integer.parseInt(new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID, LEN_STOCK_DT).trim());
//    }
//
//    public void setPTStockDT(int PTStockDT) {
//        String PTStockDT_string = Integer.toString(PTStockDT);
//        System.arraycopy(PTStockDT_string.getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID, PTStockDT_string.length());
//    }

    // MenuName
    public String getMenuName() {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE, LEN_STOCK_MENU).trim();
    }

    public void setMenuName(String menuName) {
        System.arraycopy(menuName.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE, menuName.trim().getBytes().length);
    }

    // MenuPrice
    public String getMenuPrice() {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_STOCK_MENU, LEN_STOCK_PRICE).trim();
    }

    public void setMenuPrice(String menuPrice) {
        System.arraycopy(menuPrice.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_STOCK_MENU, menuPrice.trim().getBytes().length);
    }

    // MenuAmount
    public String getMenuAmount() {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_STOCK_MENU + LEN_STOCK_PRICE, LEN_STOCK_AMOUNT).trim();
    }

    public void setMenuAmount(String menuAmount) {
        System.arraycopy(menuAmount.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_STOCK_MENU + LEN_STOCK_PRICE, menuAmount.trim().getBytes().length);
        packet[LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_STOCK_MENU + LEN_STOCK_PRICE + menuAmount.getBytes().length] = '\0';
    }

    // OrderFood
    public String getOrderFood() {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE, LEN_ORDER_FOOD).trim();
    }

    public void setOrderFood(String orderFood) {
        System.arraycopy(orderFood.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE, orderFood.getBytes().length);
    }

    // OrderAmount
    public String getOrderAmount() {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_ORDER_FOOD, LEN_ORDER_FOOD).trim();
    }

    public void setOrderAmount(String orderAmount) {
        System.arraycopy(orderAmount.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_ORDER_FOOD, orderAmount.getBytes().length);
    }

    // OrderPrice
    public String getOrderPrice() {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_ORDER_FOOD + LEN_ORDER_AMOUNT, LEN_ORDER_PRICE).trim();
    }

    public void setOrderPrice(String orderPrice) {
        System.arraycopy(orderPrice.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_ORDER_FOOD + LEN_ORDER_AMOUNT, orderPrice.getBytes().length);
    }

    // ClientBalance
    public String getClientBalance() {
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_ORDER_FOOD + LEN_ORDER_AMOUNT + LEN_ORDER_PRICE, LEN_CLIENT_BALANCE).trim();
    }

    public void setClientBalance(String clientBalance) {
        System.arraycopy(clientBalance.getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_ORDER_FOOD + LEN_ORDER_AMOUNT + LEN_ORDER_PRICE, clientBalance.getBytes().length);
        packet[LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + LEN_CLIENT_TYPE + LEN_ORDER_FOOD + LEN_ORDER_AMOUNT + LEN_ORDER_PRICE + clientBalance.getBytes().length] = '\0';
    }
//    public String getPassword(){
//        //구성으로 보아 패스워드는 byte[] 에서 로그인 아이디 바로 뒷부분에 들어가는 듯 하다.
//        return new String(packet, LEN_PROTOCOL_TYPE + LEN_LOGIN_ID, LEN_LOGIN_PASSWORD).trim();
//    }
//
//
//    public void setPassword(String password){
//        System.arraycopy(password.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE+LEN_LOGIN_ID, password.trim().getBytes().length);
//        packet[LEN_PROTOCOL_TYPE + LEN_LOGIN_ID + password.trim().getBytes().length] = '\0';
//    }
}