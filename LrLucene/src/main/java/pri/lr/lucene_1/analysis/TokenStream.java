package pri.lr.lucene_1.analysis;

import java.io.IOException;

/**
 * A TokenStream enumerates the sequence of tokens,
 * either from fields of a document or from query text.
 */
abstract public class TokenStream {
    abstract public Token next() throws IOException;
    public void close() throws IOException{}
}
