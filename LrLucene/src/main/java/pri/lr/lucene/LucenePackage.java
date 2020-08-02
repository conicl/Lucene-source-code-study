package pri.lr.lucene;

/**
 * 包含lucene的package信息
 */
public class LucenePackage {
    private LucenePackage(){}

    public static Package get() {
        return LucenePackage.class.getPackage();
    }
}
