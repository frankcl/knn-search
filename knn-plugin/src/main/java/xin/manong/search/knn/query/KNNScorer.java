package xin.manong.search.knn.query;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.Map;

/**
 * 文档及打分管理
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
public class KNNScorer extends Scorer {

    private float boost;
    private DocIdSetIterator iterator;
    private Map<Integer, Float> docScoreMap;

    public KNNScorer(Weight weight, DocIdSetIterator iterator, Map<Integer, Float> docScoreMap, float boost) {
        super(weight);
        this.iterator = iterator;
        this.docScoreMap = docScoreMap;
        this.boost = boost;
    }

    @Override
    public DocIdSetIterator iterator() {
        return iterator;
    }

    @Override
    public float getMaxScore(int upTo) throws IOException {
        return Float.MAX_VALUE;
    }

    @Override
    public float score() {
        int currentDocID = docID();
        assert currentDocID != DocIdSetIterator.NO_MORE_DOCS;
        Float score = docScoreMap.get(currentDocID);
        if (score == null) {
            throw new RuntimeException(String.format("score is null for the docID[{}]", currentDocID));
        }
        return score;
    }

    @Override
    public int docID() {
        return iterator.docID();
    }
}

