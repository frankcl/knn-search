package xin.manong.search.knn.common;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * KNN常量定义
 *
 * @author frankcl
 * @date 2023-05-10 13:47:52
 */
public class KNNConstants {

    public static final int MAX_DIMENSION = 10000;

    public static final String MAPPED_FIELD_TYPE = "knn_vector";

    public static final String FIELD_ATTRIBUTE_KNN_FIELD = "knn_field";
    public static final String FIELD_ATTRIBUTE_INDEX = "index";
    public static final String FIELD_ATTRIBUTE_DIMENSION = "dimension";

    public static final String COMPOUND_EXTENSION = "c";
    public static final String TEMP_EXTENSION = ".temp";
    public static final String HNSW_VECTOR_INDEX_DATA_EXTENSION = ".hvd";
    public static final String HNSW_VECTOR_INDEX_META_EXTENSION = ".hvm";
    public static final String FAISS_VECTOR_INDEX_DATA_EXTENSION = ".fvd";
    public static final String FAISS_VECTOR_INDEX_META_EXTENSION = ".fvm";
    public static final Set<String> KNN_VECTOR_INDEX_DATA_EXTENSIONS = ImmutableSet.of(
            HNSW_VECTOR_INDEX_DATA_EXTENSION, FAISS_VECTOR_INDEX_DATA_EXTENSION);

    public static final String HNSW_SPACE_L2 = "l2";
    public static final String HNSW_SPACE_COSINE = "cosinesimil";

    /**
     * KNN RESTFul相关常量定义
     */
    public static final String KNN_BASE_URL = "knn";
    public static final String REST_ACTION_STATS = "knn_stats_action";
    public static final String REST_ACTION_INDEX = "knn_index_action";
    public static final String REST_ACTION_WARM = "knn_warm_action";
    public static final String REST_REQUEST_NODE_ID = "node_id";
    public static final String REST_REQUEST_STAT = "stat";
    public static final String REST_REQUEST_TIMEOUT = "timeout";
    public static final String REST_REQUEST_INDEX = "index";
    public static final String REST_REQUEST_OPERATION = "operation";
    public static final String REST_RESPONSE_SIZE = "size";

    /**
     * 索引操作定义
     */
    public static final String OPERATION_VIEW = "view";
    public static final String OPERATION_EVICT = "evict";
    public static final String OPERATION_WARM = "warm";

    /**
     * 统计常量定义
     */
    public static final String HIT_COUNT = "hit_count";
    public static final String MISS_COUNT = "miss_count";
    public static final String EVICT_COUNT = "evict_count";
    public static final String LOAD_SUCCESS_COUNT = "load_success_count";
    public static final String LOAD_FAIL_COUNT = "load_fail_count";
    public static final String TOTAL_LOAD_TIME = "total_load_time";
    public static final String MEMORY_SIZE = "memory_size";
    public static final String MEMORY_STATS = "memory_stats";
    public static final String CACHE_CAPACITY_REACHED = "cache_capacity_reached";
    public static final String CIRCUIT_BREAKER_TRIGGERED = "circuit_breaker_triggered";
}
