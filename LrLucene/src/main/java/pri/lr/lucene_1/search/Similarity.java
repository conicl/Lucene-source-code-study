package pri.lr.lucene_1.search;

public class Similarity {
    private Similarity(){}

    public static final byte norm(int numTerms) {
        return (byte) Math.ceil(255.0 / Math.sqrt(numTerms));
    }
}
