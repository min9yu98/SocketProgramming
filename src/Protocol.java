import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Protocol implements Serializable {
    private static final long serialVersionUID = 1L;
    private String food;
    private int amount;
}