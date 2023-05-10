package xin.manong.search.knn.codec;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocIDMerger;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * KNN向量docValues
 *
 * @author frankcl
 * @date 2023-05-10 10:22:19
 */
public class KNNVectorDocValues extends BinaryDocValues {

    private int docID = -1;
    private KNNVectorDocValuesSub current;
    private DocIDMerger<KNNVectorDocValuesSub> docIDMerger;

    KNNVectorDocValues(DocIDMerger<KNNVectorDocValuesSub> docIDMerger) {
        this.docIDMerger = docIDMerger;
    }

    @Override
    public BytesRef binaryValue() throws IOException {
        return current.getValues().binaryValue();
    }

    @Override
    public boolean advanceExact(int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int docID() {
        return docID;
    }

    @Override
    public int nextDoc() throws IOException {
        current = docIDMerger.next();
        docID = current == null ? NO_MORE_DOCS : current.mappedDocID;
        return docID;
    }

    @Override
    public int advance(int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long cost() {
        throw new UnsupportedOperationException();
    }
}
