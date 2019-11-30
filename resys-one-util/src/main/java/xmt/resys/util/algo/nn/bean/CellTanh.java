package xmt.resys.util.algo.nn.bean;

/**
 * tanh激活函数
 */
public class CellTanh extends CellBase {
    @Override
    public double Activate(double x) {
        return Math.tanh(x);
    }

    @Override
    public double Derivative(double x) {
        double coshx = Math.cosh(x);
        double denom = (Math.cosh(2 * x) + 1);
        return 4 * coshx * coshx / (denom * denom);
    }
}
