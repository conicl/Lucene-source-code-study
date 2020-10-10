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
            other.add(lastTerm, lastTi); // add an index term
        }

        writeTerm(term);
        output.writeVint(ti.docFreq);
        output.writeVLong(ti.freqPointer - lastTi.freqPointer);
        output.writeVLong(ti.proxPointer - lastTi.proxPointer);
        if(isIndex) {
            output.writeVLong(other.output.getFilePointer() - lastIndexPointer);
            lastIndexPointer = other.output.getFilePointer();
        }

        lastTi.set(ti);
        size++;
    }

    private void writeTerm(Term term) throws IOException{
        int start = stringDifference(lastTerm.text, term.text);
        int length = term.text.length() - start;

        output.writeVint(start); // write shared prefix length
        output.writeVint(length); // write delta length
        output.writeChars(term.text, start, length);
        output.writeVint(fieldsInfos.fieldNumber(term.field)); // write field num
        lastTerm = term;
    }

    private int stringDifference(String text, String text1) {
        int len = Math.min(text.length(), text1.length());
        for(int i  = 0; i < len; i++) {
            if (text.charAt(i) != text1.charAt(i))
                return i;
        }
        return len;
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