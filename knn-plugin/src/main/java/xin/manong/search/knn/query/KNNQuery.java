package xin.manong.search.knn.query;

import lombok.Getter;
import org.apache.lucene.search.*;

/**
 * KNN查询
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
@Getter
public class KNNQuery extends Query {

    protected int k;
    protected float[] vector;
    protected String index;
    protected String field;

    public KNNQuery(int k, float[] vector,
                    String index,
                    String field) {
        this.k = k;
        this.vector = vector;
        this.index = index;
        this.field = field;
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, ScoreMode scoreMode, float boost) {
        return new KNNWeight(this, boost);
    }

    @Override
    public int hashCode() {
        return field.hashCode() ^ vector.hashCode() ^ k;
    }

    @Override
    public boolean equals(Object other) {
        return sameClassAs(other) && equalsTo(getClass().cast(other));
    }

    private boolean equalsTo(KNNQuery other) {
        return this.field.equals(other.field) && this.vector.equals(other.vector) && this.k == other.k;
    }

    @Override
    public String toString(String field) {
        return field;
    }

    @Override
    public void visit(QueryVisitor visitor) {
    }
};
