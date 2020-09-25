package pri.lr.lucene_1.document;

import java.io.Reader;

/**
 * 每个field是Document中的一个条目
 * 每个field包含两部分：name value
 * value可以是text，以String类型或者Reader传入
 * 或者是一个atomic keyword，用表示dates/url等
 * Field可以选择性store到index中，被store的子段可以在查询中返回
 */
public class Field {
    private String name = "body";
    private String stringValue = null;
    private Reader readerValue = null;
    private boolean isStored = false;
    private boolean isIndexed = true;
    private boolean isTokenized = true;

    public Field(String name, String string, boolean store, boolean index, boolean token) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (string == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        this.name = name.intern();
        this.stringValue = string;
        this.isStored = store;
        this.isIndexed = index;
        this.isTokenized = token;
    }

    public static final Field Keywords(String name, String value) {
        return new Field(name, value, true, true, false);
    }

    public static final Field UnIndexed(String name, String value) {
        return new Field(name, value, true, false, false);
    }

    public static final Field Text(String name, String value) {
        return new Field(name, value, true, true, true);
    }

    public static final Field UnStored(String name, String value) {
        return new Field(name, value, false, true, true);
    }

    public static final Field Text(String name, Reader value){
        return new Field(name, value);
    }

    Field(String name, Reader reader) {
        if (name == null) throw new IllegalArgumentException("name cannot be null");
        if (reader == null) throw new IllegalArgumentException("value cannot be null");

        this.name = name.intern();
        this.readerValue = reader;
    }

    public boolean isStored() {
        return isStored;
    }

    public boolean isIndexed() {
        return isIndexed;
    }

    public boolean isTokenized() {
        return isTokenized;
    }

    @Override
    public String toString() {
        if (isStored && isIndexed && !isTokenized)
            return "Keyword<" + name + ":" + stringValue + ">";
        else if (isStored && !isIndexed && !isTokenized)
            return "Unindexed<" + name + ":" + stringValue + ">";
        else if (isStored && isIndexed && isTokenized && stringValue!=null)
            return "Text<" + name + ":" + stringValue + ">";
        else if (!isStored && isIndexed && isTokenized && readerValue!=null)
            return "Text<" + name + ":" + readerValue + ">";
        else
            return super.toString();
    }
}
