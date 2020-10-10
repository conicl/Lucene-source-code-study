package pri.lr.lucene_1.analysis;

import java.io.Reader;

/**
 * An analyzer builds TokenStreams, which analyze text.
 *
 * it thus represents a policy for extracting index terms from text.
 *
 * Typical implementations first build a Tokenizer, which breaks the stream of
 * characters from the Reader into raw Tokens.
 * One or more TokenFilters may then be applied to the output of the Tokenizer.
 *
 */
abstract public class Analyzer {
    public TokenStream tokenStream(String filedName, Reader reader) {
        return tokenStream(reader);
    }

    public TokenStream tokenStream(Reader reader) {
        return tokenStream(null, reader);
    }
}
