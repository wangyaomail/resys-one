package xmt.resys.util.algo.nn.bean;

/**
 * 激活神经元的基类
 */
public abstract class CellBase {
    public static CellBase byType(CellType neuron_type) {
        switch (neuron_type) {
        case IDENTITY:
            return new CellIdentity();
        case TANH:
            return new CellTanh();
        case SIGMOID:
        default:
            return new CellSigmoid();
        }
    }

    /**
     * 激活函数
     */
    abstract public double Activate(double x);

    /**
     * 导数
     */
    abstract public double Derivative(double x);
}
