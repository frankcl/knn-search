package xin.manong.search.knn.common;

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
    public static final String FIELD_ATTRIBUTE_SPACE = "space";

    public static final String COMPOUND_EXTENSION = "c";
    public static final String TEMP_EXTENSION = ".temp";
    public static final String HNSW_VECTOR_INDEX_DATA_EXTENSION = ".hvd";
    public static final String HNSW_VECTOR_INDEX_META_EXTENSION = ".hvm";
    public static final String FAISS_VECTOR_INDEX_DATA_EXTENSION = ".fvd";
    public static final String FAISS_VECTOR_INDEX_META_EXTENSION = ".fvm";

    public final static String HNSW_SPACE_L2 = "l2";
    public final static String HNSW_SPACE_COSINE = "cosinesimil";
}
