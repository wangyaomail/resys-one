package xmt.resys.util.algo.nlp.trie;

public class Forest extends SmartForest<NlpWord> {
    private static final long serialVersionUID = -4616310486272978650L;

    public Forest() {};

    public Forest(char c,
                  int status,
                  NlpWord param) {
        super(c, status, param);
    }

    @Override
    public SmartForest<NlpWord> get(char c) {
        return this.getBranch(c);
    }

    @Override
    public SmartForest<NlpWord> getBranch(char c) {
        return super.getBranch(c);
    }

    /**
     * 注意这里对forest的调用是线程安全的
     */
    @Override
    public GetWord getWord(String str) {
        return getWord(str.toCharArray());
    }

    @Override
    public GetWord getWord(char[] chars) {
        return new GetWord(this, chars);
    }

    public NlpWord getParams() {
        return this.getParam();
    }
}