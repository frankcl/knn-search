package xin.manong.search.knn.index.faiss;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexType;
import xin.manong.search.knn.index.KNNResult;

import java.io.File;

/**
 * @author frankcl
 * @date 2023-01-18 16:56:10
 */
public class FAISSIndexSuite {

    @BeforeClass
    public static void init() {
        FAISSLoader.init();
    }

    @Test
    public void testReadWrite() {
        int[] ids = { 0, 1, 2 };
        float[][] data = {
                { 1.0f, 2.0f, 3.0f, 4.0f },
                { 5.0f, 6.0f, 7.0f, 8.0f },
                { 9.0f, 10.0f, 11.0f, 12.0f }
        };
        KNNIndexData indexData = new KNNIndexData(ids, data);
        FAISSIndexMeta indexMeta = new FAISSIndexMeta();
        indexMeta.num = ids.length;
        indexMeta.dimension = 4;
        indexMeta.index = "test_index";
        indexMeta.field = "test_field";
        indexMeta.type = KNNIndexType.FAISS;
        indexMeta.path = "./test_index.fvd";
        indexMeta.file = "test_index.fvd";
        indexMeta.parameterMap.put(FAISSConstants.INDEX_THREAD_QUANTITY, 2);
        FAISSIndexFactory factory = new FAISSIndexFactory();
        Assert.assertTrue(factory.buildIndex(indexData, indexMeta));

        float[] vector = { 1.0f, 1.0f, 1.0f, 1.0f };
        FAISSIndex index = new FAISSIndex(indexMeta);
        index.open();
        Assert.assertEquals(1L, index.getMemorySize());
        Assert.assertEquals(1L, index.getFileSize());
        KNNResult[] results = index.search(vector, 30);
        Assert.assertEquals(3, results.length);
        Assert.assertEquals(0, results[0].id);
        Assert.assertEquals(14.0f, results[0].score, 0.1f);
        Assert.assertEquals(1, results[1].id);
        Assert.assertEquals(126.0f, results[1].score, 0.1f);
        Assert.assertEquals(2, results[2].id);
        Assert.assertEquals(366.0f, results[2].score, 0.1f);
        index.close();
        Assert.assertTrue(new File(indexMeta.path).delete());
    }
}
