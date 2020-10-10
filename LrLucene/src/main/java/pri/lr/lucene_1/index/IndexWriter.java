package pri.lr.lucene_1.index;

import org.apache.lucene.analysis.Analyzer;
import pri.lr.lucene_1.document.Document;
import pri.lr.lucene_1.store.Directory;
import pri.lr.lucene_1.store.FSDirectory;
import pri.lr.lucene_1.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * An IndexWriter creates and maintains an index.
 *
 * documents ane added with the addDocument() method
 * when finished adding documents. close() should be called.
 *
 * if an index will not have more documents added for a while and optimal search
 * performance is desired, then the optimize() method should be called before the index is closed.
 */
public class IndexWriter {
    private Directory directory;
    private Analyzer analyzer;

    private SegmentInfos segmentInfos = new SegmentInfos(); // the segments
    private final Directory ramDirectory = new RAMDirectory(); // for temp segments

    public IndexWriter(String path, Analyzer a, boolean create) throws IOException {
        this(FSDirectory.getDirectory(path, create), a , create);
    }

    public IndexWriter(File path, Analyzer a, boolean create) throws IOException{
        this(FSDirectory.getDirectory(path, create), a, create);
    }

    public IndexWriter(Directory d, Analyzer a, boolean create) throws IOException {
        directory = d;
        analyzer = a;

        synchronized (directory) {
            if (create) {
                segmentInfos.write(directory);
            } else {
                segmentInfos.read(directory);
            }
        }
    }

    public final synchronized void close() throws IOException {
        flushRamSegments();
        ramDirectory.close();
        directory.close();
    }

    public final synchronized int docCount() {
        int count = 0;
        for (int i = 0; i < segmentInfos.size(); i++) {
            SegmentInfo si = segmentInfos.info(i);
            count += si.docCount;
        }
        return count;
    }

    public int maxFieldLength = 10000;

    public final void addDocument(Document doc) throws IOException {
        DocumentWriter dw = new DocumentWriter(ramDirectory, analyzer, maxFieldLength);
        String segmentName = newSegmentName();

        dw.addDocument(segmentName, doc); // use DocumentWriter addDocument
                                        // .fnm
                                        //
        synchronized (this) {
            segmentInfos.addElement(new SegmentInfo(segmentName, docCount(), ramDirectory));
            maybeMergeSegments();
        }
    }

    private void maybeMergeSegments() {
    }

    private final synchronized String newSegmentName() {
        return "_" + Integer.toString(segmentInfos.counter++, Character.MAX_RADIX);
    }

    /**
     * TODO
     */
    public int mergeFactor = 10;

    public int maxMergeDocs = Integer.MAX_VALUE;

    public PrintStream infoStream = null;

    public final synchronized void optimize() throws IOException {
        flushRamSegments();
        while (segmentInfos.size() > 1
                || (segmentInfos.size() == 1
                    && SegmentReader.hasDeleteions(segmentInfos.info(0)))) {
            int minSegment = segmentInfos.size() - mergeFactor;
            mergeSegments(minSegment < 0 ? 0 : minSegment);
        }
    }

    public final synchronized void addIndexes(Directory[] dirs) throws IOException{
        optimize();
        int minSegment = segmentInfos.size();
        int segmentsAddedSinceMerge = 0;
        for (int i = 0; i < dirs.length; i++) {
            SegmentInfos sis = new SegmentInfos();
            sis.read(dirs[i]);
            for (int j = 0; j < sis.size(); j++) {
                segmentInfos.addElement(sis.info(j));
            }

            if (++segmentsAddedSinceMerge == mergeFactor) {
                mergeSegments(minSegment++, false);
                segmentsAddedSinceMerge = 0;
            }
        }
        optimize();
    }

    private final void flushRamSegments() throws IOException {
        int minSegment = segmentInfos.size() - 1;
        int docCount = 0;
        while (minSegment > 0
                && (segmentInfos.info(minSegment).dir == ramDirectory)) {
            docCount += segmentInfos.info(minSegment).docCount;
            minSegment--;
        }

        if (minSegment < 0 ||
            (docCount + segmentInfos.info(minSegment).docCount) > mergeFactor ||
           !(segmentInfos.info(segmentInfos.size()-1).dir == ramDirectory)) {
            minSegment++;
        }

        if(minSegment >= segmentInfos.size()) {
            return;
        }

        mergeSegments(minSegment);
    }

    private void mergeSegments(int minSegment) {
    }

    private void mergeSegments(int minSegment, boolean delete) {
    }
}
