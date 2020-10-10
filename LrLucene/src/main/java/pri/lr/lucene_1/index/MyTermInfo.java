package pri.lr.lucene_1.index;


final public class MyTermInfo {
    int docFreq = 0; // the number of documents which contain the term
    long freqPointer = 0L;
    long proxPointer = 0L;

    public MyTermInfo() {
    }

    public MyTermInfo(int docFreq, long freqPointer, long proxPointer) {
        this.docFreq = docFreq;
        this.freqPointer = freqPointer;
        this.proxPointer = proxPointer;
    }

    public MyTermInfo(MyTermInfo ti) {
        docFreq = ti.docFreq;
        freqPointer = ti.freqPointer;
        proxPointer = ti.proxPointer;
    }



    final void set(int df, long fp, long pp) {
        docFreq = df;
        freqPointer = fp;
        proxPointer = pp;
    }


    final void set(MyTermInfo ti) {
        docFreq = ti.docFreq;
        freqPointer = ti.freqPointer;
        proxPointer = ti.proxPointer;
    }
}
