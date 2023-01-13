package xin.manong.search.knn.index;

/**
 * knn查询结果
 *
 * @author frankcl
 * @date 2023-01-10 11:26:48
 */
public class KNNResult {

    public final int id;
    public final float score;

    public KNNResult(final int id, final float score) {
        this.id = id;
        this.score = score;
    }
}
