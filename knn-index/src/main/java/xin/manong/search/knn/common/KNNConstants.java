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

    public final static String HNSW_SPACE_L2 = "l2";
    public final static String HNSW_SPACE_COSINE = "cosinesimil";
}
