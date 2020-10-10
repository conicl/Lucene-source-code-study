package pri.lr.lucene_1.index;

import pri.lr.lucene_1.store.Directory;

public class SegmentInfo {
    public String name; // uniq name in dir
    public int docCount; // number of docs in segment
    public Directory dir; // where segment resides

    public SegmentInfo(String name, int docCount, Directory dir) {
        this.name = name;
        this.docCount = docCount;
        this.dir = dir;
    }

}
