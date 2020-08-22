package pri.lr.lucene_1.store;


import java.io.IOException;

public class InputStream implements Cloneable{
    final static int BUFFER_SIZE = OutputStream.BUFFER_SIZE;

    private byte[] buffer;
    private char[] chars;

    private long bufferStart = 0; // position if file of buffer
    private int bufferLength = 0; // end of valid bytes
    private int bufferPosition = 0; // next byte to read

    protected long length;

    public final byte readByte() throws IOException {
        if (bufferPosition >= bufferLength) {
            refill();
        }

        return buffer[bufferPosition++];
    }

    public final void readBytes(byte[] b, int offset, int len) throws IOException{
        if (len < BUFFER_SIZE) {
            for (int i = 0; i < len; i++) {
                b[i + offset] = readByte();
            }
        } else {
            long start = getFilePointer();
            seekInternal(start);
            readInternal(b, offset, len);

            bufferStart = start + len;
            bufferPosition = 0;
            bufferLength = 0;
        }
    }

    public final int readInt() throws IOException {
        return  ((readByte() & 0xFF) << 24)
                | ((readByte() & 0xFF) << 16)
                | ((readByte() & 0xFF) << 8)
                | (readByte() & 0xFF);
    }

    public final int readVInt() throws IOException {
        byte b = readByte();
        int i = b & 0x7F;

        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = readByte();
            i |= (b & 0x7F) << shift;
        }

        return i;
    }
}

