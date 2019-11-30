package xmt.resys.util.algo.nlp.trie;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import xmt.resys.util.file.HBIOUtil;

public class Library {
    public static Forest makeForest(String path) throws Exception {
        return makeForest(new FileInputStream(path));
    }

    public static Forest makeForest(String path,
                                    String encoding)
            throws Exception {
        return makeForest(new FileInputStream(path), encoding);
    }

    public static Forest makeForest(InputStream inputStream) throws Exception {
        return makeForest(HBIOUtil.getReader(inputStream, "UTF-8"));
    }

    public static Forest makeForest(InputStream inputStream,
                                    String encoding)
            throws Exception {
        return makeForest(HBIOUtil.getReader(inputStream, encoding));
    }

    public static Forest makeForest(BufferedReader br) throws Exception {
        return makeLibrary(br, new Forest());
    }

    /**
     * 传入nlpword数组.构造树
     * @param values
     * @param forest
     * @return
     */
    public static Forest makeForest(List<NlpWord> words) {
        Forest forest = new Forest();
        for (NlpWord word : words) {
            insertWord(forest, word.getName(), word);
        }
        return forest;
    }

    /**
     * 词典树的构造方法
     * @param br
     * @param forest
     * @return
     * @throws Exception
     */
    private static Forest makeLibrary(BufferedReader br,
                                      Forest forest)
            throws Exception {
        if (br == null) {
            return forest;
        }
        try {
            String temp = null;
            while ((temp = br.readLine()) != null) {
                NlpWord nw = NlpWord.parse(temp);
                insertWord(forest, nw.getName(), nw);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
        return forest;
    }

    private static void insertWord(Forest forest,
                                   String temp,
                                   NlpWord param) {
        SmartForest<NlpWord> branch = forest;
        char[] chars = temp.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars.length == i + 1) {
                branch.add(new Forest(chars[i], 3, param));
            } else {
                branch.add(new Forest(chars[i], 1, null));
            }
            branch = branch.getBranch(chars[i]);
        }
    }

    /**
     * 删除一个词
     * @param forest
     * @param temp
     */
    public static void removeWord(Forest forest,
                                  String word) {
        SmartForest<NlpWord> branch = forest;
        char[] chars = word.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (branch == null) {
                return;
            }
            if (chars.length == i + 1) {
                branch.add(new Forest(chars[i], -1, null));
            }
            branch = branch.getBranch(chars[i]);
        }
    }
}