package xin.manong.search.knn.index;

/**
 * knn查询结果
 *
 * @author frankcl
 * @date 2023-01-10 11:26:48
 */
public class KNNResult {

    public int id;
    public float score;

    public KNNResult(int id, float score) {
        this.id = id;
        this.score = score;
    }
}
