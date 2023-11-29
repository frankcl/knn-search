package xin.manong.search.knn.codec.writer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author frankcl
 * @date 2023-05-30 13:47:03
 */
public class KNNSelectorSuite {

    @Test
    public void testSelectWriter() {
        {
            KNNVectorWriter writer = KNNSelector.select(600000, "test_index");
            Assert.assertTrue(writer != null);
            Assert.assertTrue(writer instanceof FAISSVectorWriter);
        }
        {
            KNNVectorWriter writer = KNNSelector.select(300000, "test_index");
            Assert.assertTrue(writer != null);
            Assert.assertTrue(writer instanceof HNSWVectorWriter);
        }
    }
}
