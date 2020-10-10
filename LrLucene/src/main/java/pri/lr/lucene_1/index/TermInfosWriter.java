package pri.lr.lucene_1.index;

import pri.lr.lucene_1.store.Directory;
import pri.lr.lucene_1.store.OutputStream;

import java.io.IOException;

public class TermInfosWriter {
    private FieldInfos fieldsInfos;
    private OutputStream output;
    private Term lastTerm = new Term("", "");
    private MyTermInfo lastTi = new MyTermInfo();
    private int size = 0;

    static final int INDEX_INTERVAL = 128;
    private long lastIndexPointer = 0L;
    private boolean isIndex = false;
    private TermInfosWriter other = null;


    public TermInfosWriter(Directory directory, String segment, FieldInfos fis) throws IOException, SecurityException {
        initialize(directory, segment, fis, false);
        other = new TermInfosWriter(directory, segment, fis, true);
        other.other = this;
    }

    private TermInfosWriter(Directory directory, String segment, FieldInfos fis,
                            boolean isIndex) throws IOException {
        initialize(directory, segment, fis, isIndex);
    }

    private void initialize(Directory directory, String segment, FieldInfos fis, boolean isi) throws IOException {
        fieldsInfos = fis;
        isIndex = isi;
        output = directory.createFile(segment + (isIndex ? ".tii" : ".tis"));
        output.writeInt(0);
    }

    public void add(Term term, MyTermInfo ti) throws IOException, SecurityException {
        if (!isIndex && term.compareTo(lastTerm) <= 0)
            throw new IOException("term out of order");
        if (ti.freqPointer < lastTi.freqPointer)
            throw new IOException("freqPointer out of order");
        if (ti.proxPointer < lastTi.proxPointer)
            throw new IOException("proxPointer out of order");

        if(!isIndex && size % INDEX_INTERVAL == 0) {
            other.add(lastTerm, lastTi);
        }

    }

    public void close() throws IOException, SecurityException{
        output.seek(0);
        output.writeInt(size);
        output.close();
        if(!isIndex) {
            other.close();
        }
    }
}