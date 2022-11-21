package socketServer.model;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class Menu {
    private List<String> food;
    private List<Integer> amount;
    private List<Integer> price;
    private static Menu menu = new Menu();
    public static Menu getInstance() {
        return menu;
    }
}
