package socketServer.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class Menu implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> food;
    private List<Integer> amount;
    private List<Integer> price;
    private static Menu menu = new Menu();
    public static Menu getInstance() {
        return menu;
    }
}
