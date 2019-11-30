package xmt.resys.util.algo.nlp.trie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NlpWord {
    private String name; // 名称
    private String nature; // 词性
    private Double weight; // 权重
    private Double num; // 出现次数
    private Double value; // 最终计算的分数，先不动

    public NlpWord newWord() {
        return new NlpWord(name, nature, weight, 0.0, 0.0);
    }

    public NlpWord numPP() {
        num++;
        return this;
    }

    public static NlpWord parse(String... params) {
        NlpWord nw = null;
        try {
            if (params != null && params.length > 0) {
                nw = new NlpWord();
                nw.setName(params[0]);
                if (params.length > 1) {
                    nw.setValue(Double.parseDouble(params[1]));
                }
            }
        } catch (Exception e) {
        }
        return nw;
    }
}
