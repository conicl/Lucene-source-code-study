package pri.lr.lucene_1.store;


import java.io.IOException;

/**
 * Abstract class for output from a file in a Directory
 */
abstract public class OutputStream {
    final static int BUFFER_SIZE = 1024;

    private final byte[] buffer = new byte[BUFFER_SIZE];
    private long bufferStart = 0; // position in file of buffer
    private int bufferPosition = 0; // position in buffer

    public final void writeByte(byte b) throws IOException {
        if (bufferPosition >= BUFFER_SIZE) {
            flush();
        }

        buffer[bufferPosition++] = b;
    }

    public final void writeBytes(byte[] b, int length) throws IOException {
        for (byte oneByte : b ) {
            writeByte(oneByte);
        }
    }

    public final void writeInt(int i) throws IOException {
        /**
         *  ____ ____ ____ ____ ____ ____ ____ ____
         *  先写高位
         *  再写低位
         */
        writeByte((byte) (i >> 24));
        writeByte((byte) (i >> 16));
        writeByte((byte) (i >> 8));
        writeByte((byte) i);
    }

    public final void writeVint(int i) throws IOException {
        while ((i & ~0x7F) != 0) { // i & 1000 0000
            writeByte((byte) ((i & 0x7f) | 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
    }

    public final void writeLong(long i) throws IOException {
        writeInt((int) (i >> 32));
        writeInt((int) i);
    }

    public final void writeVLong(long i) throws IOException {
        while ( (i & ~0x7F) != 0) {
            writeByte((byte) ((i & 0x7f) & 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
    }

    public final void writeString(String s) throws IOException {
        int len = s.length();
        writeVint(len);
        writeChars(s, 0, len);
    }

    public final void writeChars(String s, int start, int length) throws IOException {
        final int end = start + length;
        for (int i = start; i < end; i++) {
            final int code = s.charAt(i);
            if (code >= 0x01 && code <= 0x7F) {
                writeByte((byte) code);
            } else if (((code >= 0x80) && (code <= 0x7FF)) || code == 0) {
                writeByte((byte)(0xC0 | (code >> 6)));
                writeByte((byte)(0x80 | (code & 0x3F)));
            }else {
                writeByte((byte)(0xE0 | (code >>> 12)));
                writeByte((byte)(0x80 | ((code >> 6) & 0x3F)));
                writeByte((byte)(0x80 | (code & 0x3F)));
            }
        }
    }

    protected final void flush() throws IOException { // flush 是刷缓存
        flushBuffer(buffer, bufferPosition);
        bufferStart += bufferPosition;
        bufferPosition = 0;
    }

    abstract protected void flushBuffer(byte[] b, int len) throws IOException;

    public void close() throws IOException {
        flush();
    }

    public final long getFilePointer() throws IOException {
        return bufferStart + bufferPosition;
    }

    public void seek(long pos) throws IOException {
        flush();
        bufferStart = pos; // ？？？ 直接等于？
    }

    abstract public long length() throws IOException;

}
