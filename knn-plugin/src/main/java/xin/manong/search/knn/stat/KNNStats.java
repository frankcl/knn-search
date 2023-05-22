package xin.manong.search.knn.stat;

import java.util.HashMap;
import java.util.Map;

/**
 * KNN统计项集合
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
public class KNNStats {

    private Map<String, KNNStat<?>> statMap;

    public KNNStats(Map<String, KNNStat<?>> statMap) {
        this.statMap = statMap;
    }

    /**
     * 获取统计项Map
     *
     * @return 统计项Map
     */
    public Map<String, KNNStat<?>> getStatMap() {
        return statMap;
    }

    /**
     * 根据key获取统计项
     *
     * @param key 统计项key
     * @return 返回存在统计项， 否则抛出异常
     * @throws IllegalArgumentException
     */
    public KNNStat<?> getStat(String key) {
        if (!statMap.containsKey(key)) {
            throw new RuntimeException(String.format("stat is not found for key[%s]", key));
        }
        return statMap.get(key);
    }

    /**
     * 获取结点统计项Map
     *
     * @return 结点统计项集合
     */
    public Map<String, KNNStat<?>> getNodeStatMap() {
        return getStatMap(false);
    }

    /**
     * 获取集群统计项Map
     *
     * @return 集群统计项Map
     */
    public Map<String, KNNStat<?>> getClusterStatMap() {
        return getStatMap(true);
    }

    /**
     * 获取集群或结点级别统计项Map
     *
     * @param clusterLevel true集群级别，false结点级别
     * @return 集群或结点级别统计项Map
     */
    private Map<String, KNNStat<?>> getStatMap(Boolean clusterLevel) {
        Map<String, KNNStat<?>> statsMap = new HashMap<>();
        for (Map.Entry<String, KNNStat<?>> entry : this.statMap.entrySet()) {
            if (entry.getValue().isClusterLevel() == clusterLevel) {
                statsMap.put(entry.getKey(), entry.getValue());
            }
        }
        return statsMap;
    }
}