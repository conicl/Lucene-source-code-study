package pri.lr.lucene_1.store;

import java.io.IOException;

abstract public class Directory {

    abstract public OutputStream createFile(String name)  throws IOException, SecurityException;
    abstract public InputStream openFile(String name) throws IOException, SecurityException;

}
