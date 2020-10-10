package pri.lr.lucene_1.analysis;

import java.io.IOException;

abstract public class TokenFilter extends TokenStream{
    protected TokenStream input;

    @Override
    public void close() throws IOException {
        input.close();
    }
}
