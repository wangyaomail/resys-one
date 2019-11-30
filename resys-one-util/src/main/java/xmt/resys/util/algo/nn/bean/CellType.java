package xmt.resys.util.algo.nn.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 激活函数的种类
 */
@Getter
@AllArgsConstructor
public enum CellType {
    SIGMOID(1, "sigmoid"),
    IDENTITY(2, "identity"),
    TANH(3, "tanh"),;
    private Integer index;
    private String name;

    public static CellType valueOf(int index) {
        for (CellType cell : values()) {
            if (cell.getIndex() == index) {
                return cell;
            }
        }
        return null;
    }
}
