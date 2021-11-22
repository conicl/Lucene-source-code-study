package pri.lr.lucene_1.index;

public final class Term {
    String field;
    String text;

    public Term(String field, String text) {
        this.field = field;
        this.text = text;
    }

    public Term(String field, String text, boolean intern) {
        this.field = intern ? field.intern() : field;
        this.text = text;
    }

    public final String field() {
        return field;
    }

    public final String text() {
        return text;
    }

    public final boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        Term other = (Term) o;
        return field == other.field && text.equals(other.text);
    }

    /** Combines the hashCode() of the field and the text. */
    public final int hashCode() {
        return field.hashCode() + text.hashCode();
    }

    public final int compareTo(Term other) {
        if (field == other.field)			  // fields are interned
            return text.compareTo(other.text);
        else
            return field.compareTo(other.field);
    }

    public final void set(String fld, String txt) {
        field = fld;
        text = txt;
    }

    public final String toString() {
        return "Term<" + field + ":" + text + ">";
    }

}
