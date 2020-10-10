package pri.lr.lucene_1.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import pri.lr.lucene_1.document.Document;
import pri.lr.lucene_1.document.Field;
import pri.lr.lucene_1.search.Similarity;
import pri.lr.lucene_1.store.Directory;
import pri.lr.lucene_1.store.OutputStream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;

public class DocumentWriter {
    private Analyzer analyzer;
    private Directory directory;
    private FieldInfos fieldInfos;
    private int maxFieldLength;

    DocumentWriter(Directory d, Analyzer a, int mfl) {
        directory = d;
        analyzer = a;
        maxFieldLength = mfl;
    }

    final void addDocument(String segment, Document doc) throws IOException {
        // write Field names
        fieldInfos = new FieldInfos();
        fieldInfos.add(doc);
        fieldInfos.write(directory, segment + ".fnm");

        // write Field values
        FieldsWriter fieldsWriter = new FieldsWriter(directory, segment, fieldInfos);
        try {
            fieldsWriter.addDocument(doc); // 填充field信息
        } finally {
            fieldsWriter.close();
        }

        // invert doc into postingTable
        postingTable.clear(); // clear posting table
        filedLengths = new int[fieldInfos.size()]; // init fieldLengths
        invertDocument(doc);

        // sort postingTable into an array
        MyPosting[] postings = sortPostingTable(); //  pai xu
         /*
    for (int i = 0; i < postings.length; i++) {
      Posting posting = postings[i];
      System.out.print(posting.term);
      System.out.print(" freq=" + posting.freq);
      System.out.print(" pos=");
      System.out.print(posting.positions[0]);
      for (int j = 1; j < posting.freq; j++)
	System.out.print("," + posting.positions[j]);
      System.out.println("");
    }
    */

        // write postings
        writePostings(postings, segment);

        // write norms of indexed fields
        writeNorms(doc, segment);

    }

    private void writeNorms(Document doc, String segment) throws IOException {
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            if(field.isIndexed()) {
                int fieldNumber = fieldInfos.fieldNumber(field.getName());
                OutputStream norm = directory.createFile(segment + ".f" + fieldNumber);
                try {
                    norm.writeByte(Similarity.norm(filedLengths[fieldNumber]));
                } finally {
                    norm.close();
                }
            }
        }
    }

    private void writePostings(MyPosting[] postings, String segment) throws IOException {
        OutputStream freq = null;
        OutputStream prox = null;
        TermInfosWriter tis = null;

        try {
            freq = directory.createFile(segment + ".frq");
            prox = directory.createFile(segment + ".prx");
            tis = new TermInfosWriter(directory, segment, fieldInfos);
            MyTermInfo ti = new MyTermInfo();

            for (int i = 0; i < postings.length; i++) {
                MyPosting posting = postings[i];

                ti.set(1, freq.getFilePointer(), prox.getFilePointer());
                tis.add(posting.term, ti);

                int f = posting.freq;
                if (f == 1) {
                    freq.writeVint(1);
                } else {
                    freq.writeVint(0);
                    freq.writeVint(f);
                }
                int lastPosition = 0;
                int[] positions = posting.positions;
                for (int j = 0; j < f; j++) {
                    int position = positions[j];
                    prox.writeVint(position - lastPosition);
                    lastPosition = position;
                }
            }
        } finally {
            if(freq != null) freq.close();
            if(prox != null) prox.close();
            if(tis != null) tis.close();
        }
    }

    /**
     * sort positingTable into an array
     * @return
     */
    private final MyPosting[] sortPostingTable() {
        MyPosting[] array = new MyPosting[postingTable.size()];
        Enumeration postings = postingTable.elements();
        for (int i = 0; postings.hasMoreElements(); i++) {
            array[i] = (MyPosting) postings.nextElement();
        }

        quickSort(array, 0, array.length - 1);

        return array;
    }

    private void quickSort(MyPosting[] array, int lo, int high) {
        if(lo >= high) {
            return;
        }

        int mid = (lo + high ) / 2;
        if(array[lo].term.compareTo(array[mid].term) > 0) {
            MyPosting tmp = array[lo];
            array[lo] = array[mid];
            array[lo] = tmp;
        }

        if(array[mid].term.compareTo(array[high].term) > 0) {
            MyPosting tmp = array[mid];
            array[mid] = array[high];
            array[high] = tmp;

            if (array[lo].term.compareTo(array[mid].term) > 0) {
                MyPosting tmp2 = array[lo];
                array[lo] = array[mid];
                array[mid] = tmp2;
            }
        }

        int left = lo + 1;
        int right = high - 1;

        if (left  >= right) {
            return;
        }

        Term partition = array[mid].term;

        for(;;) {
            while (array[right].term.compareTo(partition) > 0) {
                --right;
            }

            while (left < right && array[left].term.compareTo(partition) <= 0) {
                ++left;
            }

            if(left < right) {
                MyPosting tmp = array[left];
                array[left] = array[right];
                array[right] = tmp;
                --right;
            } else {
                break;
            }
        }

    }

    private final void invertDocument(Document doc) throws IOException{
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            String fieldName = field.getName();
            int fieldNumber = fieldInfos.fieldNumber(fieldName);

            /**
             * fieldLengths 初始化时是长度为filedInfos.size 初值为0的int数组
             */
            int position = filedLengths[fieldNumber]; // position in field
            if(field.isIndexed()) {
                if(!field.isTokenized()) {
                    addPosition(fieldName, field.getStringValue(), position++);
                } else {
                    Reader reader;
                    if(field.getReaderValue() != null) {
                        reader = field.getReaderValue();
                    } else if (field.getStringValue() != null) {
                        reader = new StringReader(field.getStringValue());
                    } else {
                        throw new IllegalArgumentException("field must have either String or Reader value");
                    }

                    TokenStream stream = analyzer.tokenStream(fieldName, reader);
                    try {
                        for (Token t = stream.next(); t != null; t= stream.next()) {
                            addPosition(fieldName, t.termText(), position++);
                            if(position > maxFieldLength) {
                                break;
                            }
                        }
                    } finally {
                        stream.close();
                    }
                }
                filedLengths[fieldNumber] = position; // save field length
            }
        }

    }
    private final Term termBuffer = new Term("", ""); // avoid consing
    private final void addPosition(String fieldName, String termText, int position) {
        termBuffer.set(fieldName, termText);
        MyPosting ti = (MyPosting) postingTable.get(termBuffer);
        if (ti != null) {
            int freq = ti.freq;
            if (ti.positions.length == freq) { // position array is full
                int[] newPositions = new int[freq * 2];
                int[] postions = ti.positions;
                for (int i = 0; i < freq; i++) {
                    newPositions[i] = postions[i];
                }
                ti.positions = newPositions;
            }
            ti.positions[freq] = position; // 每出现一个，就在ti.positions中追加一个position
            // 所以出现的频率就等于positions的实际数据长度
            ti.freq = freq + 1;
        } else {
            Term term = new Term(fieldName, termText, false);
            postingTable.put(term, new MyPosting(term, position));
        }
    }

    // Keys are terms, values are Postings.
    // Used to buffer a document before it is written to the index.
    private final Hashtable postingTable = new Hashtable();
    private int[] filedLengths;

}

final class MyPosting {
    Term term;
    int freq; // its frequency in doc
    int[] positions; // positions it occurs at

    public MyPosting(Term term,int position) {
        this.term = term;
        this.freq = 1;
        this.positions = new int[1];
        positions[0] = position;
    }
}