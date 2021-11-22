package pri.lr.lucene_1.index;

import org.apache.lucene.document.Field;
import pri.lr.lucene_1.document.Document;
import pri.lr.lucene_1.store.Directory;
import pri.lr.lucene_1.store.InputStream;
import pri.lr.lucene_1.store.OutputStream;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class FieldInfos {
    private Vector byNumber = new Vector();
    private Hashtable byName = new Hashtable();

    FieldInfos(){
        add("",false);
    }
    FieldInfos(Directory diectory, String name) throws IOException {
        InputStream inputStream = diectory.openFile(name);
        try{
            read(inputStream);
        } finally {
            inputStream.close();
        }
    }

    final void add(Document doc) {
        // add document
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            add(field.name(), field.isIndexed());
        }
    }

    final void add(FieldInfos other) {
        for (int i = 0; i< other.size(); i++) {
            FieldInfo fi = other.fieldInfo(i);
            add(fi.name, fi.isIndexed);
        }
    }

    private final void add(String name, boolean isIndexed) {
        FieldInfo fi = fieldInfo(name);
        if(fi == null) {
            addInternal(name, isIndexed);
        } else if(fi.isIndexed != isIndexed) {
            throw new IllegalStateException("field " + name +
                    (fi.isIndexed ? "must" : "cannot") + " be an indexed field."); // keep same with old exist field
        }
    }

    private final void addInternal(String name, boolean isIndexed){
        FieldInfo fi = new FieldInfo(name, isIndexed, byNumber.size());
        byNumber.add(fi);
        byName.put(name,fi);
    }

    final int fieldNumber(String fieldName) {
        FieldInfo fi = fieldInfo(fieldName);
        if(fi != null) {
            return fi.number;
        } else {
            return -1;
        }
    }

    final FieldInfo fieldInfo(String fieldName) {
        return (FieldInfo) byName.get(fieldName);
    }

    final String fieldName(int fieldNumber) {
        return fieldInfo(fieldNumber).name;
    }

    final FieldInfo fieldInfo(int filedNumber) {
        return (FieldInfo) byNumber.elementAt(filedNumber);
    }

    final int size() {
        return byNumber.size();
    }

    final void write(Directory d, String name) throws IOException {
        OutputStream outputStream = d.createFile(name);
        try {
            write(outputStream);
        } finally {
            outputStream.close();
        }
    }

    final void write(OutputStream outputStream) throws IOException {
        outputStream.writeVint(size());
        for(int i = 0; i < size(); i++) {
            FieldInfo fi = fieldInfo(i);
            outputStream.writeString(fi.name);
            outputStream.writeByte((byte) (fi.isIndexed ? 1 : 0));
        }
    }

    private final void read(InputStream input) throws IOException {
        int size = input.readVInt();
        for (int i = 0; i < size; i++) {
            addInternal(input.readString().intern(), input.readByte() != 0);
        }
    }
}
