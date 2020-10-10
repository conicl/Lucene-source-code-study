package pri.lr.lucene_1.demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import pri.lr.lucene_1.index.IndexWriter;

import java.io.IOException;

public class Demo {
    public static void main(String[] args) {
        String indexDirectory = "./demo_index_dir";
        Analyzer analyzer = new StandardAnalyzer();
        try {
            IndexWriter indexWriter = new IndexWriter(indexDirectory, analyzer, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
