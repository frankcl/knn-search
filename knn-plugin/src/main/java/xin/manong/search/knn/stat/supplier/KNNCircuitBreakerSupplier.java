package xin.manong.search.knn.stat.supplier;

import xin.manong.search.knn.common.KNNSettings;

import java.util.function.Supplier;

/**
 * 集群内容熔断状态获取supplier
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
public class KNNCircuitBreakerSupplier implements Supplier<Boolean> {

    @Override
    public Boolean get() {
        return KNNSettings.isCircuitBreakerTriggered();
    }
}