package pri.lr.lucene_1.store;

import java.io.IOException;

abstract public class Directory {

    /**
     * 返回directory中的所有文件
     * @return
     * @throws IOException
     * @throws SecurityException
     */
    abstract public String[] list() throws IOException, SecurityException;

    abstract public boolean fileExists(String name) throws IOException, SecurityException;

    abstract public long fileModified(String name) throws IOException, SecurityException;

    abstract public void deleteFile(String name) throws IOException, SecurityException;

    abstract public void renameFile(String from, String to) throws IOException, SecurityException;

    abstract public long fileLength(String name) throws IOException, SecurityException;
    abstract public OutputStream createFile(String name)  throws IOException, SecurityException;
    abstract public InputStream openFile(String name) throws IOException, SecurityException;
    abstract public void close() throws IOException, SecurityException;
}
