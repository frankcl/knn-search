package xin.manong.search.knn.util;

import java.util.List;

/**
 * KNN工具
 *
 * @author frankcl
 * @date 2023-05-17 13:58:02
 */
public class KNNUtil {

    /**
     * 对象列表转化浮点数列表
     *
     * @param objects 对象列表
     * @return 浮点数列表
     */
    public static float[] objectsToFloats(List<Object> objects) {
        float[] vector = new float[objects.size()];
        for (int i = 0; i < objects.size(); i++) vector[i] = ((Number) objects.get(i)).floatValue();
        return vector;
    }

    /**
     * 计算cosine距离
     *
     * @param vector1
     * @param vector2
     * @return cosine距离
     */
    public static float cosineScore(float[] vector1, float[] vector2) {
        float m = 0f, s1 = 0f, s2 = 0f;
        for (int k = 0; k < vector1.length; k++) {
            m += vector1[k] * vector2[k];
            s1 += vector1[k] * vector1[k];
            s2 += vector2[k] * vector2[k];
        }
        s1 = (float) Math.sqrt(s1);
        s2 = (float) Math.sqrt(s2);
        return 1f - m / (s1 * s2);
    }
}
