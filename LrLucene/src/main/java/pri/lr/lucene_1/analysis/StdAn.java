package pri.lr.lucene_1.analysis;

import java.io.Reader;

public class StdAn extends Analyzer{
    @Override
    public TokenStream tokenStream(String filedName, Reader reader) {

        return super.tokenStream(filedName, reader);
    }

    @Override
    public TokenStream tokenStream(Reader reader) {
        return super.tokenStream(reader);
    }
}
