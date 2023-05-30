package xin.manong.search.knn.plugin;

import org.elasticsearch.index.codec.CodecService;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.engine.EngineConfig;
import org.elasticsearch.index.engine.EngineFactory;
import org.elasticsearch.index.engine.InternalEngine;
import xin.manong.search.knn.codec.KNNCodecService;

/**
 * KNNEngineFactory
 * 使用KNNCodecService支持向量索引构建
 *
 * @author frankcl
 * @date 2020-07-01 19:38:11
 */
public class KNNEngineFactory implements EngineFactory {

    private static CodecService codecService = new KNNCodecService();

    @Override
    public Engine newReadWriteEngine(EngineConfig config) {
        EngineConfig engineConfig = new EngineConfig(config.getShardId(), config.getThreadPool(),
                config.getIndexSettings(), config.getWarmer(), config.getStore(), config.getMergePolicy(),
                config.getAnalyzer(), config.getSimilarity(), codecService, config.getEventListener(),
                config.getQueryCache(), config.getQueryCachingPolicy(), config.getTranslogConfig(),
                config.getFlushMergesAfter(), config.getExternalRefreshListener(), config.getInternalRefreshListener(),
                config.getIndexSort(), config.getCircuitBreakerService(), config.getGlobalCheckpointSupplier(),
                config.retentionLeasesSupplier(), config.getPrimaryTermSupplier(), config.getTombstoneDocSupplier());
        return new InternalEngine(engineConfig);
    }
}
