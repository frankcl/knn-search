package xin.manong.search.knn.index.hnsw;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexType;
import xin.manong.search.knn.index.KNNResult;

import java.io.File;

/**
 * @author frankcl
 * @date 2023-01-14 19:15:38
 */
public class HNSWIndexSuite {

    @BeforeClass
    public static void init() {
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

        HNSWIndex index = new HNSWIndex(indexMeta);
        index.open();
        Assert.assertEquals(1L, index.getFileSize());
        Assert.assertEquals(1L, index.getMemorySize());
        {
            KNNResult[] results = index.search(data[0], 1);
            Assert.assertEquals(1, results.length);
            Assert.assertEquals(1, results[0].id);
            Assert.assertEquals(0f, results[0].score, 0.1f);
        }
        {
            KNNResult[] results = index.search(data[1], 1);
            Assert.assertEquals(1, results.length);
            Assert.assertEquals(2, results[0].id);
            Assert.assertEquals(0f, results[0].score, 0.1f);
        }
        {
            KNNResult[] results = index.search(data[2], 1);
            Assert.assertEquals(1, results.length);
            Assert.assertEquals(3, results[0].id);
            Assert.assertEquals(0f, results[0].score, 0.1f);
        }
        index.close();
        Assert.assertTrue(new File(indexMeta.path).delete());
    }
}
