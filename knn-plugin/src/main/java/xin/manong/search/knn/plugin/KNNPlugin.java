package xin.manong.search.knn.plugin;

import com.google.common.collect.ImmutableList;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.settings.*;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.NodeEnvironment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.engine.EngineFactory;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.plugins.*;
import org.elasticsearch.repositories.RepositoriesService;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.watcher.ResourceWatcherService;
import xin.manong.search.knn.monitor.KNNCircuitBreakerMonitor;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.common.KNNSettings;
import xin.manong.search.knn.mapper.KNNVectorFieldMapper;
import xin.manong.search.knn.query.KNNQueryBuilder;
import xin.manong.search.knn.rest.action.KNNStatsAction;
import xin.manong.search.knn.rest.action.KNNStatsTransportNodesAction;
import xin.manong.search.knn.rest.handler.KNNStatsRESTHandler;
import xin.manong.search.knn.stat.KNNStats;
import xin.manong.search.knn.stat.KNNStatsConfig;

import java.util.*;
import java.util.function.Supplier;

/**
 * KNN插件
 *
 * @author frankcl
 * @date 2023-05-18 14:20:57
 */
public class KNNPlugin extends Plugin implements MapperPlugin,
        SearchPlugin, ActionPlugin, EnginePlugin {

    private KNNStats knnStats;

    @Override
    public Map<String, Mapper.TypeParser> getMappers() {
        return Collections.singletonMap(KNNConstants.MAPPED_FIELD_TYPE, new KNNVectorFieldMapper.TypeParser());
    }

    @Override
    public List<QuerySpec<?>> getQueries() {
        return Collections.singletonList(new QuerySpec<>(KNNQueryBuilder.QUERY_NAME,
                KNNQueryBuilder::new, KNNQueryBuilder::fromXContent));
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return Arrays.asList(
                new ActionHandler<>(KNNStatsAction.INSTANCE, KNNStatsTransportNodesAction.class)
        );
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings,
                                             RestController restController,
                                             ClusterSettings clusterSettings,
                                             IndexScopedSettings indexScopedSettings,
                                             SettingsFilter settingsFilter,
                                             IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {
        return Arrays.asList(
                new KNNStatsRESTHandler(settings, restController, knnStats)
        );
    }

    @Override
    public Collection<Object> createComponents(
            Client client, ClusterService clusterService, ThreadPool threadPool,
            ResourceWatcherService resourceWatcherService, ScriptService scriptService,
            NamedXContentRegistry xContentRegistry, Environment environment,
            NodeEnvironment nodeEnvironment, NamedWriteableRegistry namedWriteableRegistry,
            IndexNameExpressionResolver indexNameExpressionResolver,
            Supplier<RepositoriesService> repositoriesServiceSupplier) {
        KNNSettings.getInstance().initialize(client, clusterService);
        KNNIndexCache.getInstance().setResourceWatcherService(resourceWatcherService);
        KNNCircuitBreakerMonitor circuitBreakerMonitor = new KNNCircuitBreakerMonitor(
                threadPool, clusterService, client);
        circuitBreakerMonitor.start();
        knnStats = new KNNStats(KNNStatsConfig.KNN_STATS);
        return ImmutableList.of(knnStats);
    }

    @Override
    public List<Setting<?>> getSettings() {
        return KNNSettings.getSettings();
    }

    @Override
    public Optional<EngineFactory> getEngineFactory(IndexSettings indexSettings) {
        return Optional.of(new KNNEngineFactory());
    }
}
