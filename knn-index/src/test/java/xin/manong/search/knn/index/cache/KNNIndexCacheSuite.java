package xin.manong.search.knn.index.cache;

import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.common.KNNSettings;
import xin.manong.search.knn.index.KNNIndex;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.KNNIndexType;
import xin.manong.search.knn.index.faiss.FAISSIndexFactory;
import xin.manong.search.knn.index.faiss.FAISSIndexMeta;

import java.io.File;

/**
 * @author frankcl
 * @date 2023-05-29 11:04:51
 */
public class KNNIndexCacheSuite extends ESSingleNodeTestCase {

    private KNNIndexMeta indexMeta;
    private KNNIndexCache cache;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        prepare();
        KNNSettings.getInstance().initialize(this.client(), null);
        cache = KNNIndexCache.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        cache.destroy();
        destroy();
        super.tearDown();
    }

    @Test
    public void testCacheOperations() throws Exception {
        KNNIndex knnIndex = cache.get(indexMeta);
        Assert.assertTrue(knnIndex != null);
        Assert.assertTrue(knnIndex.getMemorySize() == 1L);
        Assert.assertTrue(cache.getCacheMemorySizeKB() == 1L);
        Assert.assertTrue(cache.getCacheMemorySizeKB(indexMeta.index) == 1L);

        cache.remove(indexMeta.path);
        Assert.assertTrue(cache.getCacheMemorySizeKB() == 0L);
    }

    private void prepare() {
        int[] ids = { 0, 1, 2 };
        float[][] data = {
                { 1.0f, 2.0f, 3.0f, 4.0f },
                { 5.0f, 6.0f, 7.0f, 8.0f },
                { 9.0f, 10.0f, 11.0f, 12.0f }
        };
        KNNIndexData indexData = new KNNIndexData(ids, data);
        indexMeta = new FAISSIndexMeta();
        indexMeta.num = ids.length;
        indexMeta.dimension = 4;
        indexMeta.index = "test_index";
        indexMeta.field = "test_field";
        indexMeta.type = KNNIndexType.FAISS;
        indexMeta.path = "./test_index.fvd";
        indexMeta.file = "test_index.fvd";
        FAISSIndexFactory factory = new FAISSIndexFactory();
        Assert.assertTrue(factory.buildIndex(indexData, indexMeta));
    }

    private void destroy() {
        Assert.assertTrue(new File(indexMeta.path).delete());
    }
}
