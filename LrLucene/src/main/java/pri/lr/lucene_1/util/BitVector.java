package pri.lr.lucene_1.util;

import pri.lr.lucene_1.store.Directory;
import pri.lr.lucene_1.store.InputStream;
import pri.lr.lucene_1.store.OutputStream;

import java.io.IOException;

public class BitVector {
    public byte[] bits; // this is public just so that will inline.
    private int size;
    private int count = -1;

    public BitVector(int n) {
        this.size = n;
        bits = new byte[size >> 3 + 1];
    }

    // bit -> 17
    // bit   bin    bit&7   1 << (bit & 7)
    // 0    0000    0000    0001
    // 1    0001    0001    0010
    // 2    0010    0010    0100
    // 3    0011    0011    1000
    // 4    0100    0100
    // 5    0101    0101
    // 6    0110    0110
    // 7    0111    0111
    public final void set(int bit) {
        bits[bit >> 3] |= 1 << (bit & 7);
        count = -1;
    }

    /** Sets the value of <code>bit</code> to zero. */
    public final void clear(int bit) {
        bits[bit >> 3] &= ~(1 << (bit & 7));
        count = -1;
    }

    public final boolean get(int bit) {
        return (bits[bit >> 3] & (1 << (bit & 7))) != 0;
    }

    public final int size() {
        return size;
    }

    public final int count() {
        if (count == -1) {
            int c = 0;
            int end = bits.length;
            for (byte bit : bits) {
                c = c + BYTE_COUNTS[bit & 0xFF];
            }
            count = c;
        }
        return count;
    }


    private static final byte[] BYTE_COUNTS = {	  // table of bits/byte
            0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7,
            4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8
    };

    public final void write(Directory d, String name) throws IOException {
        OutputStream outputStream = d.createFile(name);
        try {
            outputStream.writeInt(size());
            outputStream.writeInt(count);
            outputStream.writeBytes(bits, bits.length);
        } finally {
            outputStream.close();
        }
    }

    /**
     *
     * @param d
     * @param name
     * @throws IOException
     */
    public BitVector(Directory d, String name) throws IOException {
        InputStream input = d.openFile(name);
        try {
            size = input.readInt();
            count = input.readInt();
            bits = new byte[(size >> 3) + 1];
            input.readBytes(bits, 0, bits.length);
        } finally {
            input.close();
        }

    }

}
