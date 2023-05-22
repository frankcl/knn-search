package xin.manong.search.knn.stat;

import java.util.function.Supplier;

/**
 * KNN统计项
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
public class KNNStat<T> {
    private Boolean clusterLevel;
    private Supplier<T> supplier;

    public KNNStat(Boolean clusterLevel, Supplier<T> supplier) {
        this.clusterLevel = clusterLevel;
        this.supplier = supplier;
    }

    /**
     * 是否集群级别统计项
     *
     * @return 集群级别统计项返回true，否则返回false
     */
    public Boolean isClusterLevel() { return clusterLevel; }

    /**
     * 获取统计项值
     *
     * @return 统计项值
     */
    public T getValue() {
        return supplier.get();
    }
}