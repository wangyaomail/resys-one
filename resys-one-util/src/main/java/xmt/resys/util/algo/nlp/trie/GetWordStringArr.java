package xmt.resys.util.algo.nlp.trie;

/**
 * 基本的string【】 类
 * @author ansj
 */
public class GetWordStringArr extends SmartGetWord<String[]> {
    public GetWordStringArr(ForestStringArr forest,
                            char[] chars) {
        super(forest, chars);
    }

    public GetWordStringArr(ForestStringArr forest,
                            String content) {
        super(forest, content);
    }

    public String getParam(int i) {
        final String[] param = this.getParam();
        if (param == null || i >= param.length) {
            return null;
        } else {
            return param[i];
        }
    }

    public String[] getParams() {
        return this.getParam();
    }
}
