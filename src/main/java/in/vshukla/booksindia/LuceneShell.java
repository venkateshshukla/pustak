package in.vshukla.booksindia;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by venkatesh on 30/4/17.
 */
public class LuceneShell {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneShell.class);

    public static void main(String[] args) throws IOException {
        LuceneShell luceneShell = new LuceneShell();
        String idxDirStr = AppUtils.getProperty(AppConstants.PROP_IDX_LOC, () -> AppConstants.DEFAULT_IDX_LOC);
        luceneShell.initialize(idxDirStr);
    }

    private LuceneShell() {
    }

    private String shellSymbol;

    private Scanner scanner = new Scanner(System.in);

    private SearcherManager searcherManager;

    private void initialize (String idxDirStr) throws IOException {
        AppUtils.blankStringCheck(idxDirStr, "Lucene : Cannot initialize a blank directory name.");
        assert AppUtils.directoryExists(idxDirStr) : "Directory does not exists : " + idxDirStr;

        Path idxPath = Paths.get(idxDirStr);
        Directory idxDir = MMapDirectory.open(idxPath);
        assert DirectoryReader.indexExists(idxDir) : "No indices present in the directory : " + idxDirStr;

        searcherManager = new SearcherManager(idxDir, null);
        LOGGER.info("Initialized index reader from the directory {}", idxDir);
        LOGGER.info("Number of entries in the index : {}", executeAgainstReader(IndexReader::numDocs));

        shellSymbol = AppUtils.getProperty(AppConstants.PROP_PS2, () -> AppConstants.DEFAULT_PS2);
        printOutput(getUsage(), "\n");
        while(true) {
            printShell();
            String query = getNextQuery();
            try {
               List<Document> resultDocList = getQueryResult(query, 25);
               if (resultDocList == null) {
                   continue;
               }
               if (resultDocList.isEmpty()) {
                   printOutput("No results found.\n");
                   continue;
               }
               resultDocList.forEach(doc -> printDoc(doc));
            } catch (ParseException e) {
                LOGGER.error("Shell : Error parsing query {}", query, e);
            }

        }
    }

    private void printDoc(Document doc) {
        assert doc != null : "Cannot print null doc";
        doc.getFields().forEach(f -> printOutput(String.format("%s=%s, ", f.name(), doc.get(f.name()))));
        printOutput("\n");
    }

    private static final QueryParser PARSER = new QueryParser("title", new StandardAnalyzer());

    private List<Document> getQueryResult (String queryStr, final int numResults) throws ParseException {
        final Query query = PARSER.parse(queryStr);
        return getQueryResult(query, numResults);
    }

    private List<Document> getQueryResult (Map<String, String> queryMap, final int numResults) {
        final PhraseQuery.Builder queryBuilder = new PhraseQuery.Builder();
        queryMap.forEach((k, v) -> queryBuilder.add(new Term(AppUtils.cleanValue(k), AppUtils.cleanValue(v))));
        final Query query = queryBuilder.build();
        return getQueryResult(query, numResults);
    }

    private List<Document> getQueryResult (final Query query, final int numResults) {
        return (List<Document>) executeAgainstSearcher((searcher) -> getResultDocs(searcher, query, numResults));
    }

    private List<Document> getResultDocs (IndexSearcher searcher, Query query, int numResults) {
        try {
            TopDocs topDocs = searcher.search(query, numResults);
            return Arrays.stream(topDocs.scoreDocs)
                    .map(scoreDoc -> getResultDoc(searcher, scoreDoc.doc))
                    .filter(doc -> doc != null)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Shell : Caught IO Exception", e);
        }
        return null;
    }

    private Document getResultDoc (IndexSearcher searcher, int docId) {
        try {
            return searcher.doc(docId);
        } catch (IOException e) {
            LOGGER.error("Shell : Caught an IO Exception", e);
        }
        return null;
    }

    private String getNextQuery () {
        return scanner.hasNext() ? scanner.next().trim() : null;
    }

    private void printShell () {
        printOutput(shellSymbol);
    }

    private void printOutput (String... str) {
        Arrays.stream(str).forEach(System.out::print);
    }

    private String getUsage() {
        assert searcherManager != null : "Shell : SearcherManager is not initialized.";
        return "Available fields : " + ((List<String>) executeAgainstSearcher((is) -> getIndexFieldsQuietly(is)))
                .stream().collect(Collectors.joining(", "));
    }

    private List<String> getIndexFieldsQuietly (IndexSearcher indexSearcher) {
        try {
            return getIndexFields(indexSearcher);
        } catch (IOException e) {
            LOGGER.error("Shell : Caught IO Exception", e);
        }
        return null;
    }

    private List<String> getIndexFields (IndexSearcher indexSearcher) throws IOException {
        TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), 1);
        assert topDocs.scoreDocs.length == 1 : "Shell : No results found";
        Document doc = indexSearcher.doc(topDocs.scoreDocs[0].doc);
        assert doc != null : "Shell : Fetched a null doc";
        return doc.getFields().stream().map(IndexableField::name).collect(Collectors.toList());
    }

    private Object executeAgainstReader(Function<IndexReader, Object> func) throws IOException {
        assert searcherManager != null : "Shell : SearcherManager is not initialized.";
        try {
            IndexSearcher indexSearcher = searcherManager.acquire();
            try {
                return func.apply(indexSearcher.getIndexReader());
            } finally {
                searcherManager.release(indexSearcher);
            }
        } catch (IOException e) {
            LOGGER.error("Shell : Caught an IO Exception");
        }
        return null;
    }

    private Object executeAgainstSearcher(Function<IndexSearcher, Object> func) {
        assert searcherManager != null : "Shell : SearcherManager is not initialized.";
        try {
            IndexSearcher indexSearcher = searcherManager.acquire();
            try {
                return func.apply(indexSearcher);
            } finally {
                searcherManager.release(indexSearcher);
            }
        } catch (IOException e) {
            LOGGER.error("Shell : Caught an IO Exception");
        }
        return null;
    }
}
