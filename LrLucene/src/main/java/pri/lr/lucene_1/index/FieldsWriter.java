package pri.lr.lucene_1.index;

import pri.lr.lucene_1.document.Document;
import pri.lr.lucene_1.document.Field;
import pri.lr.lucene_1.store.Directory;
import pri.lr.lucene_1.store.OutputStream;

import java.io.IOException;
import java.util.Enumeration;

public class FieldsWriter {
    private FieldInfos fieldInfos;
    private OutputStream fieldsStream;
    private OutputStream indexStream;

    FieldsWriter(Directory d, String segment, FieldInfos fn) throws IOException {
        fieldInfos = fn;
        fieldsStream = d.createFile(segment + ".fdt");
        indexStream = d.createFile(segment + ".fdx");
    }

    final void close() throws IOException {
        fieldsStream.close();
        indexStream.close();
    }

    final void addDocument(Document doc) throws IOException {
        indexStream.writeLong(fieldsStream.getFilePointer());// 在.fdx文件中写入.fdt文件的当前文件指针位置

        int storedCount = 0;
        Enumeration fields = doc.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            if (field.isStored()) {
                storedCount++;
            }
        }
        fieldsStream.writeVint(storedCount); // .fdt文件写入stored的field个数
        fields = doc.fields(); //
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            if(field.isStored()) {
                fieldsStream.writeVint(fieldInfos.fieldNumber(field.getName())); // .fdt中写入每个stored的field的fieldNumber

                byte bits = 0;
                if (field.isTokenized()) {
                    bits |= 1;
                }
                fieldsStream.writeByte(bits);
                fieldsStream.writeString(field.getStringValue());
            }
        }
    }
}
