package pri.lr.lucene_1.document;

import java.util.Enumeration;

/**
 * Doucument
 *
 *
 */
public final class Document {
    DocumentFieldList fieldList = null;

    /* create a new document without no fields */
    Document(){}

    public final void add(Field field) {
        fieldList = new DocumentFieldList(field, fieldList);
    }

    public final Field getField(String name) {
        for (DocumentFieldList list = fieldList; list != null; list = list.next) {
            if (list.field.getName().equals(name)) {
                return list.field;
            }
        }
        return null;
    }

    public final String get(String name) {
        Field field = getField(name);
        if (field != null) {
            return field.getStringValue();
        }
        return null;
    }

    public final Enumeration fields() {
        return new DocumentFieldEnumeration(this);
    }

    public final String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Document<");
        for (DocumentFieldList list = fieldList; list != null; list = list.next) {
            buffer.append(list.field.toString());
            if (list.next != null) {
                buffer.append(" ");
            }
        }
        buffer.append(">");
        return buffer.toString();
    }
}

// tou cha fa
// DocumentFieldList ==== Node
final class DocumentFieldList {
    DocumentFieldList(Field f, DocumentFieldList n) {
        field = f;
        next = n;
    }

    Field field;
    DocumentFieldList next;
}

final class DocumentFieldEnumeration implements Enumeration {
    DocumentFieldList fields;

    DocumentFieldEnumeration(Document d) {
        fields = d.fieldList;
    }

    @Override
    public boolean hasMoreElements() {
        return fields != null;
    }

    @Override
    public Object nextElement() {
        Field result = fields.field;
        fields = fields.next;
        return result;
    }
}