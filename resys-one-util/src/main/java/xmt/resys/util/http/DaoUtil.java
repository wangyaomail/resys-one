package xmt.resys.util.http;

import java.lang.reflect.Method;

public class DaoUtil {
    public static void closeIfValid(Object linker) {
        if (linker != null) {
            try {
                Method closeMethod = linker.getClass().getMethod("close");
                if (closeMethod != null) {
                    closeMethod.invoke(linker);
                }
            } catch (Exception e) {
            }
        }
    }
}
