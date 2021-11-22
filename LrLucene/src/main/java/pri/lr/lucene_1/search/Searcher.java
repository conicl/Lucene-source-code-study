package pri.lr.lucene_1.search;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;

import java.io.IOException;

public abstract class Searcher {

    public final Hits search(Query query) throws IOException {
        return search(query, null);
    }
    public final Hits search(Query query, Filter filter ) throws IOException {
        return new Hits(this, query, filter);
    }
}
