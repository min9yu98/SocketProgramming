package socketServer;

import java.util.HashMap;

public class Client {
    private static Client client = new Client();
    private static HashMap<String, Integer> clientList;
    private Client() {
        clientList = new HashMap<String, Integer>();
    }

    public static Client getInstance() {
        return client;
    }

    public boolean addClient(String id) {
        if (checkId(id)) {
            return false;
        } else {
            clientList.put(id, 0);
            return true;
        }
    }

    public boolean checkId(String id) {
        return clientList.containsKey(id);
    }

    public int getPoint(String id) {
        return clientList.get(id);
    }

    public void setPoint(String id, int point) {
        clientList.replace(id, clientList.get(id) + point);
    }

    public void subPoint(String id, int point) {
        clientList.replace(id, clientList.get(id) - point);
    }
}
