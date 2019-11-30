package xmt.resys.common.bean.job;

import java.lang.reflect.Method;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用于Controller执行任务的对象
 */
@Data
@AllArgsConstructor
public class ControllerJob {
    private String key;
    private Method method;
    private Object service;

    public Object execute() throws Exception {
        return method.invoke(service);
    }
}
