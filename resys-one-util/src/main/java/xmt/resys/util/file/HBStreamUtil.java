package xmt.resys.util.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HBStreamUtil {
    // 把一个inputstream进行复制
    public static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
