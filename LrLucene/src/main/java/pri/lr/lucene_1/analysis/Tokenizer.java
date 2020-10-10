package pri.lr.lucene_1.analysis;

import java.io.IOException;
import java.io.Reader;

abstract public class Tokenizer extends TokenStream{
    protected Reader input;
    public void close() throws IOException {
        input.close();
    }
}
