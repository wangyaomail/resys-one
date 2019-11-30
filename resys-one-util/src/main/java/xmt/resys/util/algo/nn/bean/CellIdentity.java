package xmt.resys.util.algo.nn.bean;

/**
 * y=x
 */
public class CellIdentity extends CellBase {
    @Override
    public double Activate(double x) {
        return x;
    }

    @Override
    public double Derivative(double x) {
        return 1;
    }
}
