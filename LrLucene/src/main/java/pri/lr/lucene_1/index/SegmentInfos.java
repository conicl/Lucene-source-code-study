package pri.lr.lucene_1.index;

import pri.lr.lucene_1.store.Directory;
import pri.lr.lucene_1.store.InputStream;
import pri.lr.lucene_1.store.OutputStream;

import java.io.IOException;
import java.util.Vector;

public class SegmentInfos extends Vector {
    public int counter = 0;
    public final SegmentInfo info(int i) {
        return (SegmentInfo) elementAt(i);
    }

    public final void read(Directory directory) throws IOException {
        InputStream input = directory.openFile("segments");
        try {
            counter = input.readInt();
            for(int i = input.readInt(); i > 0; i--) {
                SegmentInfo si = new SegmentInfo(input.readString(), input.readInt(), directory);
                addElement(si);
            }
        }finally {
            input.close();
        }
    }
    public final void write(Directory directory) throws IOException {
        OutputStream outputStream = directory.createFile("segment.new");
        try{
            outputStream.writeInt(counter);
            outputStream.writeInt(size()); // how many info
            for(int i = 0; i < size(); i++) {
                SegmentInfo si = info(i);
                outputStream.writeString(si.name);
                outputStream.writeInt(si.docCount);
            }
        } finally {
            outputStream.close();
        }
        directory.renameFile("segment.new", "segments");
    }
}
