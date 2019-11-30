package xmt.resys.util.algo.nlp.trie;

/**
 * 基本的string【】 类
 */
public class GetWord extends SmartGetWord<NlpWord> {
    public GetWord(Forest forest,
                   char[] chars) {
        super(forest, chars);
    }

    public GetWord(Forest forest,
                   String content) {
        super(forest, content);
    }
}
