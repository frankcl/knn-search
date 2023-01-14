package xin.manong.search.knn.index.hnsw;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexType;

/**
 * @author frankcl
 * @date 2023-01-14 19:15:38
 */
public class HNSWIndexSuite {

    @BeforeClass
    public static void init() {
        System.out.println(System.getProperty("java.library.path"));
        System.out.println(System.getProperty("java.security.policy"));
        HNSWLoader.init();
    }

    @Test
    public void testReadWrite() throws Exception {
        int[] ids = new int[] { 1, 2, 3 };
        float[][] data = new float[3][];
        data[0] = new float[] { 1f, 1f, 1f };
        data[1] = new float[] { 2f, 2f, 2f };
        data[2] = new float[] { 3f, 3f, 3f };
        KNNIndexData indexData = new KNNIndexData(ids, data);
        HNSWIndexMeta indexMeta = new HNSWIndexMeta();
        indexMeta.efConstruction = 512;
        indexMeta.efSearch = 512;
        indexMeta.indexThreadQty = 1;
        indexMeta.space = "l2";
        indexMeta.M = 16;
        indexMeta.index = "test_index";
        indexMeta.dimension = 3;
        indexMeta.field = "test_field";
        indexMeta.num = 3;
        indexMeta.path = "./test_index.hnsw";
        indexMeta.type = KNNIndexType.HNSW;
        HNSWIndexFactory factory = new HNSWIndexFactory();
        Assert.assertTrue(factory.buildIndex(indexData, indexMeta));
    }
}
