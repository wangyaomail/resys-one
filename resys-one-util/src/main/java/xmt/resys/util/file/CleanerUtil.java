package xmt.resys.util.file;

import java.io.File;

public class CleanerUtil {
    public static void cleanFolderByTime(String path,
                                         long expireTime) {
        if (path != null && path.equals("/") && path.equals("/data/zzti/static")) {
            File root = new File(path);
            File[] filearray = root.listFiles();
            if (filearray != null && filearray.length > 0) {
                delteFile(new File(path), expireTime);
            }
        }
    }

    public static void delteFile(File file,
                                 long expireTime) {
        File[] filearray = file.listFiles();
        if (filearray != null) {
            if (filearray.length == 0) {
                if (file.lastModified() < expireTime) {
                    file.delete();
                }
            } else {
                for (File f : filearray) {
                    if (f.lastModified() < expireTime) {
                        if (f.isDirectory()) {
                            delteFile(f, expireTime);
                        } else {
                            f.delete();
                        }
                    }
                }
            }
        }
    }
}
