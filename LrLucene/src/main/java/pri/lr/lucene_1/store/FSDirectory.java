package pri.lr.lucene_1.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;

public class FSDirectory extends Directory{

    /**
     * 用于保证per path拥有唯一的 Directory 实例
     * readers和writers就可以同步
     *
     * 这里应该用个WeakHashMap, 可以被GC回收
     * 这里使用 refCounts
     */
    private static final Hashtable DIRECTORIES = new Hashtable();

    /**
     * 返回 location 对应的Directory实例
     *
     * 由于使用DIRECTORIES做了cache，因此对于相同的path，可以返回相同的实例，从而可以进行同步
     * @return
     * @throws IOException
     */
    public static FSDirectory getDirectory(String path, boolean create) throws IOException {
        return getDirectory(new File(path), create);
    }

    public static FSDirectory getDirectory(File file, boolean create) throws IOException {
        file = new File(file.getCanonicalPath()); // 返回一个canonical pathname，both absolute and unique
        FSDirectory dir;

        synchronized (DIRECTORIES) {
            dir = (FSDirectory) DIRECTORIES.get(file);
            if (dir == null) {
                dir = new FSDirectory(file, create);
                DIRECTORIES.put(file, dir);
            }
        }

        synchronized (dir) {
            dir.refCount++;
        }

        return dir;
    }

    private File dirctory = null;
    private int refCount;

    private FSDirectory(File path, boolean create) throws IOException {
        dirctory = path; // private方法，执行该方法时，path对应的Directory实例时不存在的，直接赋值
        if (!dirctory.exists() && create) {
            dirctory.mkdir();
        }

        if (!dirctory.isDirectory()) {
            throw new IOException(path + " not a directory");
        }

        if (create) {
            String[] files = dirctory.list();
            for (String f : files) {
                File file = new File(dirctory, f); // 会创建新的文件
                if (!file.delete()) {
                    throw new IOException("couldn't delete " + f);
                }
            }
        }
    }

    @Override
    public String[] list() throws IOException, SecurityException {
        return dirctory.list();
    }

    @Override
    public boolean fileExists(String name) throws IOException, SecurityException {
        File file = new File(dirctory, name);
        return file.exists();
    }

    /**
     * 返回文件last modify的时间
     * @param name
     * @return
     * @throws IOException
     * @throws SecurityException
     */
    @Override
    public long fileModified(String name) throws IOException, SecurityException {
        File file = new File(dirctory, name);
        return file.lastModified();
    }

    @Override
    public void deleteFile(String name) throws IOException, SecurityException {
        File file = new File(dirctory, name);
        if (!file.delete()) {
            throw new IOException("couldn't delete " + name);
        }
    }

    @Override
    public final synchronized void renameFile(String from, String to) throws IOException, SecurityException {
        File old = new File(dirctory, from);
        File nu = new File(dirctory, to);

        if (nu.exists()) {
            if (!nu.delete()) {
                throw new IOException("couldn't delete " + to);
            }
        }

        if (!old.renameTo(nu)) {
            throw new IOException("couldn't rename " + from + " to " + to);
        }
    }

    @Override
    public long fileLength(String name) throws IOException, SecurityException {
        File file = new File(dirctory, name);
        return file.length();
    }

    /**
     * 创建一个新的空文件
     * @param name
     * @return
     * @throws IOException
     * @throws SecurityException
     */
    @Override
    public OutputStream createFile(String name) throws IOException, SecurityException {
        return new FSOutputStream(new File(dirctory, name));
    }


    @Override
    public InputStream openFile(String name) throws IOException, SecurityException {
        return new FSInputStream(new File(dirctory, name));
    }

    @Override
    public final synchronized void close() throws IOException, SecurityException {
        if (--refCount <= 0) {
            synchronized (DIRECTORIES) {
                DIRECTORIES.remove(dirctory);
            }
        }
    }
}

final class FSInputStream extends InputStream {
    private class Descriptor extends RandomAccessFile {
        public long position;
        public Descriptor(File file, String mode) throws IOException {
            super(file, mode);
        }
    }

    Descriptor file = null;
    boolean isClone;

    public FSInputStream(File path) throws IOException {
        file = new Descriptor(path, "r");
        length = file.length();
    }

    protected final void readInternal(byte[] b, int offset, int len) throws IOException {
        synchronized (file) {
            long position = getFilePointer();
            if (position != file.position) {
                file.seek(position);
                file.position = position;
            }

            int total = 0;
            do {
                int i = file.read(b, offset + total, len - total);
                if (i == -1) {
                    throw new IOException("read past EOF");
                }
                file.position += i;
                total += i;
            } while (total < len);
        }
    }

    public final void close() throws IOException {
        if (!isClone) {
            file.close();
        }
    }

    protected final void seekInternal(long position) throws IOException {

    }

    protected final void finalize() throws IOException {
        close();
    }

    public Object clone() {
        FSInputStream clone = (FSInputStream) super.clone();
        clone.isClone = true;
        return clone;
    }
}

final class FSOutputStream extends OutputStream {
    RandomAccessFile file = null;
    public FSOutputStream(File path) throws IOException {
        if (path.isFile()) {
            throw new IOException(path + " already exists ");
        }

        file = new RandomAccessFile(path, "rw");
    }

    public final void flushBuffer(byte[] b, int size) throws IOException {
        file.write(b, 0, size);
    }

    public final void close() throws IOException {
        super.close();
        file.close();
    }

    public final void seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
    }

    public final long length() throws IOException {
        return file.length();
    }

    protected final void finalize() throws IOException {
        file.close();
    }
}

/*
* DIRECTORIES map用于cache来保证每个path有唯一的实例
* 可以在reader和writer之间同步
*
* openfile是直接创建？
*/
