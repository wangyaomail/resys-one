package xmt.resys.util.algo.nn.bean;

/**
 * sigmoid激活函数
 */
public class CellSigmoid extends CellBase {
    @Override
    public double Activate(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * 导数
     */
    @Override
    public double Derivative(double x) {
        double act = Activate(x);
        return act * (1 - act);
    }
}
