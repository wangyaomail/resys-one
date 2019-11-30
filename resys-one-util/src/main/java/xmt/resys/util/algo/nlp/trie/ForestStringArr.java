package xmt.resys.util.algo.nlp.trie;

public class ForestStringArr extends SmartForest<String[]> {
    private static final long serialVersionUID = -4616310486272978650L;

    public ForestStringArr() {};

    public ForestStringArr(char c,
                           int status,
                           String[] param) {
        super(c, status, param);
    }

    @Override
    public SmartForest<String[]> get(char c) {
        return this.getBranch(c);
    }

    @Override
    public SmartForest<String[]> getBranch(char c) {
        return super.getBranch(c);
    }

    /**
     * 注意这里对forest的调用是线程安全的
     */
    @Override
    public GetWordStringArr getWord(String str) {
        return getWord(str.toCharArray());
    }

    @Override
    public GetWordStringArr getWord(char[] chars) {
        return new GetWordStringArr(this, chars);
    }

    public String[] getParams() {
        return this.getParam();
    }
}