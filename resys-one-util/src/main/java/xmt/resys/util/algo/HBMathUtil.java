package xmt.resys.util.algo;

import xmt.resys.util.time.TimeUtil;

public class HBMathUtil {
    /**
     * 计算sigmoid通用方法
     */
    public static double sigmoid(double x,
                                 double step,
                                 double scale,
                                 double shift,
                                 double offset) {
        return scale / (1.0 + Math.exp(-(x * 1.0 + offset) / step)) + shift;
    }

    /**
     * 0->0, 5->0.8, oo->1
     */
    public static double sigmoid5(double x) {
        return sigmoid(x, 1.5399, 1.3679, -0.3679, -1.5399);
    }

    /**
     * 0->0, 10->0.5, oo->1
     */
    public static double sigmoid10(double x) {
        return (double) sigmoid(x, 6.45, 1.3679, -0.3679, -6.45);
    }

    /**
     * 0->0, 1->0.9, oo->1
     */
    public static double sigmoid1(double x) {
        return sigmoid(x, 0.2825, 1.3679, -0.3679, -0.2825);
    }

    public static double calStepScore(double x,
                                      double step) {
        return sigmoid(x, step, 2.0, -1.0, 0.0);
    }

    public static double normAddTwoScore(double a,
                                         double b) {
        return Math.sqrt(a * a + b * b);
    }

    public static double calFreshness(long currentTime,
                                      long tarTime) {
        int dayDelay = TimeUtil.getTimeNumBetweenTwoTime(tarTime, currentTime, 1);
        int hourDelay = TimeUtil.getTimeNumBetweenTwoTime(tarTime, currentTime, 2);
        int minDelay = TimeUtil.getTimeNumBetweenTwoTime(tarTime, currentTime, 3);
        double freshness = (double) (Math.pow(0.9, dayDelay) + Math.pow(0.99, hourDelay)
                + Math.pow(0.9999, minDelay));
        freshness = (double) HBMathUtil.sigmoid(freshness, 0.3, 1, 0, -1.4);
        return freshness;
    }

    public static double getKeyByMid(double mid,
                                     double will) {
        return -mid / (Math.log(1.3679 / (will + 0.3679) - 1.0) - 1);
    }

    public static double sigmoidToFlat(double x,
                                       double key) {
        return sigmoid(x, key, 1.3679, -0.3679, -key);
    }
}
