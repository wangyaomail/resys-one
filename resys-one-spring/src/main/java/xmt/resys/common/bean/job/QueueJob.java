package xmt.resys.common.bean.job;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用于Queue执行的任务对象
 */
@Data
@AllArgsConstructor
public class QueueJob implements Serializable {
    private static final long serialVersionUID = -2207858059496459507L;
    private Object input;
    private Method method;
    private Object service;

    /**
     * 如果这里报错了，记得检查传入的方法返回值是否是boolean
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public boolean execute() throws Exception {
        return (boolean) method.invoke(service, input);
    }
}
