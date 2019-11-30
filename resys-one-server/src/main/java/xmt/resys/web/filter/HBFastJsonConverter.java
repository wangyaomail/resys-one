package xmt.resys.web.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

import xmt.resys.util.file.HBStreamUtil;

public class HBFastJsonConverter extends FastJsonHttpMessageConverter {
    protected static final Logger logger = LoggerFactory.getLogger(HBFastJsonConverter.class);

    @Override
    protected Object readInternal(Class<? extends Object> clazz,
                                  HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        ByteArrayOutputStream baos = HBStreamUtil.cloneInputStream(inputMessage.getBody());
        String inputMsg = new String(baos.toByteArray(), "utf-8").trim();
        if (inputMsg.startsWith("{") && inputMsg.endsWith("}")) {
            // 很有可能传来的信息是json
            try { // 如果是json，进行格式化后再输出
                inputMsg = JSONObject.parse(inputMsg).toString();
            } catch (Exception e) {
            }
        }
        logger.info(inputMsg);
        HttpInputMessage newInputMessage = new MyHttpInputMessage(inputMessage) {
            @Override
            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream(baos.toByteArray());
            }
        };
        Object obj = super.readInternal(clazz, newInputMessage);
        filterObject(clazz, obj);
        return obj;
    }

    abstract class MyHttpInputMessage implements HttpInputMessage {
        private HttpInputMessage inputMessage;

        public MyHttpInputMessage(HttpInputMessage inputMessage) {
            this.inputMessage = inputMessage;
        }

        @Override
        public HttpHeaders getHeaders() {
            return inputMessage.getHeaders();
        }
    }

    public void filterObject(Class<? extends Object> clazz,
                             Object obj) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null || value.toString().length() == 0) {
                    field.set(obj, null);
                }
            } catch (Exception e) {
                logger.error("null convert error.", e);
            }
        }
    }
}
