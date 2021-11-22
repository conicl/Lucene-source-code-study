package pri.lr.lucene_1.index;

public class FieldInfo {
    String name;
    boolean isIndexed;
    int number;

    FieldInfo(String na, boolean tk, int nu) {
        name = na;
        isIndexed = tk;
        number = nu;
    }
}
