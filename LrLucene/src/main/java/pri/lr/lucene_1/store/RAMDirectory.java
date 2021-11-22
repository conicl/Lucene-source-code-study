package pri.lr.lucene_1.store;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Vector;

public class RAMDirectory extends Directory{
    Hashtable files = new Hashtable();

    public RAMDirectory() {

    }

    public final String[] list() {
        String[] result = new String[files.size()];
        int i = 0;
        Enumeration names = files.keys();
        while (names.hasMoreElements()) {
            result[i++] = (String) names.nextElement();
        }
        return result;
    }

    @Override
    public boolean fileExists(String name) throws IOException, SecurityException {
        RAMFile file = (RAMFile) files.get(name);
        return file != null;
    }

    @Override
    public long fileModified(String name) throws IOException, SecurityException {
        RAMFile file = (RAMFile) files.get(name);
        return file.lastModified;
    }

    @Override
    public void deleteFile(String name) throws IOException, SecurityException {
        files.remove(name);
    }

    @Override
    public void renameFile(String from, String to) throws IOException, SecurityException {
        RAMFile file = (RAMFile) files.get(from);
        files.remove(from);
        files.put(to, file);
    }


    @Override
    public long fileLength(String name) throws IOException, SecurityException {
        RAMFile file = (RAMFile) files.get(name);
        return file.length;
    }

    @Override
    public OutputStream createFile(String name) throws IOException, SecurityException {
        RAMFile file = new RAMFile();
        files.put(name, file);
        return new RAMOutputStream(file);
    }

    @Override
    public InputStream openFile(String name) throws IOException, SecurityException {
        RAMFile file = (RAMFile) files.get(name);
        return new RAMInputStream(file);
    }

    @Override
    public final void close() throws IOException, SecurityException {

    }
}

/**
 * RAMFile
 */
final class RAMFile {
    Vector buffers = new Vector();
    long length;
    long lastModified = System.currentTimeMillis();
}

final class RAMInputStream extends InputStream implements Cloneable {
    RAMFile file;
    int pointer = 0;

    public RAMInputStream(RAMFile file) {
        this.file = file;
        length = file.length;
    }

    /**
     * 有可能不在一个buffer
     * 用了两个buffer
     * @param dest
     * @param destOffset
     * @param length
     * @throws IOException
     */
    @Override
    protected void readInternal(byte[] dest, int destOffset, int length) throws IOException {
        int bufferNumber = pointer/InputStream.BUFFER_SIZE;
        int bufferOffset = pointer%InputStream.BUFFER_SIZE;

        int bytesInBuffer = InputStream.BUFFER_SIZE - bufferOffset;
        int bytesToCopy = bytesInBuffer >= length ? length : bytesInBuffer;

        byte[] buffer = (byte[]) file.buffers.elementAt(bufferNumber);
        System.arraycopy(buffer, bufferOffset, dest, destOffset, bytesToCopy);

        if (bytesToCopy < length) {
            destOffset += bytesToCopy;
            bytesToCopy = length - bytesToCopy;
            buffer = (byte[]) file.buffers.elementAt(bufferNumber + 1);
            System.arraycopy(buffer, 0, dest, destOffset, bytesToCopy);
        }
        pointer += length;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    protected void seekInternal(long pos) throws IOException {
        pointer = (int) pos;
    }
}

final class RAMOutputStream extends OutputStream {
    RAMFile file;
    int pointer = 0;

    public RAMOutputStream(RAMFile file) {
        this.file = file;
    }

    @Override
    protected void flushBuffer(byte[] src, int len) throws IOException {
        int bufferNumber = pointer / OutputStream.BUFFER_SIZE;
        int bufferOffset = pointer % OutputStream.BUFFER_SIZE;

        int bytesInBuffer = OutputStream.BUFFER_SIZE - bufferOffset;
        int bytesToCopy = bytesInBuffer >= len ? len : bytesInBuffer;

        if (bufferNumber == file.buffers.size()) {
            file.buffers.addElement(new byte[OutputStream.BUFFER_SIZE]);
        }

        byte[] buffer = (byte[]) file.buffers.elementAt(bufferNumber);
        System.arraycopy(src, 0, buffer, bufferOffset, bytesToCopy);

        if (bytesToCopy < len) {
            int srcOffset = bytesInBuffer;
            bytesToCopy = len = bytesInBuffer;
            bufferNumber++;
            if (bufferNumber == file.buffers.size()) {
                file.buffers.addElement(new byte[OutputStream.BUFFER_SIZE]);
            }

            buffer = (byte[]) file.buffers.elementAt(bufferNumber);
            System.arraycopy(src, srcOffset, buffer, 0, bytesToCopy);
        }

        pointer += len;

        if (pointer > file.length) {
            file.length = pointer;
        }

        file.lastModified = System.currentTimeMillis();
    }

    public final void see(long pos) throws IOException {
        super.seek(pos);
        pointer = (int) pos;
    }

    @Override
    public long length() throws IOException {
        return file.length;
    }

    public final void close() throws IOException {
        super.close();
    }
}
