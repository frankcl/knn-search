package xin.manong.search.knn.index.faiss;

/**
 * FAISS常量定义
 *
 * @author frankcl
 * @date 2023-01-17 15:08:01
 */
public class FAISSConstants {

    public static final String M = "M";
    public static final String EF_SEARCH = "efSearch";
    public static final String EF_CONSTRUCTION = "efConstruction";
    public static final String QUANTIZE_NUM = "quantizeNum";
    public static final String SUB_QUANTIZE_NUM = "subQuantizeNum";
    public static final String CENTROID_NUM = "centroidNum";
    public static final String N_PROBE = "nProbe";
    public static final String PCA_DIMENSION = "pcaDimension";
    public static final String ENCODE_BITS = "encodeBits";


    /**
     * 前缀组件
     */
    public final static String COMPONENT_PREFIX_ID_MAP = "IDMap";
    /**
     * 转换组件
     */
    public final static String COMPONENT_TRANSFORM_PCA = "PCA";
    /**
     * 搜索组件
     */
    public final static String COMPONENT_SEARCH_IVF = "IVF";
    public final static String COMPONENT_SEARCH_IMI = "IMI";
    public final static String COMPONENT_SEARCH_HNSW = "HNSW";
    /**
     * 编码组件
     */
    public final static String COMPONENT_ENCODE_PQ = "PQ";
    public final static String COMPONENT_ENCODE_FLAT = "Flat";
}
