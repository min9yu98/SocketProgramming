import lombok.Getter;
import lombok.Setter;

import java.io.OutputStream;
import java.io.Serializable;

@Getter
@Setter
public class Unit implements Serializable {
    private static final long serialVersionUID = 1L;
    private String food;
    private int amount;
    private static Unit unit = new Unit();

    public static Unit getInstance() {
        return unit;
    }
}
