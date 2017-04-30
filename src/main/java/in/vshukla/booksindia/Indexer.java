package in.vshukla.booksindia;

import in.vshukla.booksindia.exceptions.AppRuntimeException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

/**
 *
 * Created by venkatesh on 29/4/17.
 */
public class Indexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);

    private IndexWriter indexWriter;

    public void initialize () throws IOException {
        String idxDirStr = AppUtils.getProperty(AppConstants.PROP_IDX_LOC, () -> AppConstants.DEFAULT_IDX_LOC);
    }

    public void initialize (String idxDirStr) throws IOException {
        AppUtils.blankStringCheck(idxDirStr, "Index : Cannot work with a blank directory.");
        if (!AppUtils.directoryExists(idxDirStr)) {
            throw new AppRuntimeException("Index : Given directory does not exist " + idxDirStr);
        }
        Directory directory = MMapDirectory.open(Paths.get(idxDirStr));

        // Creating a writerConfig with StandardAnalyzer. Can be changed suiting the needs.
        IndexWriterConfig writerConfig = new IndexWriterConfig();
        // Also, openmode can be set using writerConfig.setOpenMode()
        // This is the default value
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        indexWriter = new IndexWriter(directory, writerConfig);
        LOGGER.info("Indexer initialized.");
    }

    /**
     * Add the given map as a document to the lucene index.
     *
     * @param map Key-value pairs to be added to the index. The key would become the field name.
     * @throws IOException In case of low-level IO errors.
     */
    public void insert (Map<String, String> map) throws IOException {
        assert indexWriter != null : "IndexWriter is uninitialized. Initialize it before inserting.";
        LOGGER.info("Indexing {}", map);
        final Document document = new Document();
        map.forEach((k, v) -> document.add(new StringField(k, v, Field.Store.YES)));
        indexWriter.addDocument(document);
    }

    /**
     * Add the given object as a document to the lucene index.
     *
     * @param object Object to be indexed.
     * @throws IOException In case of low-level IO errors.
     */
    public void insert (Object object) throws IOException {
        assert indexWriter != null : "IndexWriter is uninitialized. Initialize it before inserting.";
        insert(AppUtils.getMapFromObject(object));
    }
}
