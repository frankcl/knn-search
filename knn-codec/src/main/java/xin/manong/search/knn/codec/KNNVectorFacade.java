package xin.manong.search.knn.codec;

/**
 * KNN向量封装
 *
 * @author frankcl
 * @date 2023-05-10 14:25:30
 */
public class KNNVectorFacade {

    /**
     * 文档ID数组
     */
    public int[] docIDs;
    /**
     * 向量数组
     */
    public float[][] vectors;

    public KNNVectorFacade(int[] docIDs, float[][] vectors) {
        this.docIDs = docIDs;
        this.vectors = vectors;
    }
}
