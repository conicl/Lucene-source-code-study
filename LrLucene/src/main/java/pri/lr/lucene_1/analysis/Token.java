package pri.lr.lucene_1.analysis;

/**
 *
 */
public final class Token {
    public String getTermText() {
        return termText;
    }

    String termText; // the text of the term
    int startOffset; // start in source text
    int endOffset; // end in source text
    String type = "word"; // lexical type



}
