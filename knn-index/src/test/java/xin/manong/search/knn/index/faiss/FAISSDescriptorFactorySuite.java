package xin.manong.search.knn.index.faiss;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author frankcl
 * @date 2023-01-18 16:14:14
 */
public class FAISSDescriptorFactorySuite {

    @Test
    public void testMake() {
        {
            FAISSIndexMeta meta = new FAISSIndexMeta();
            meta.dimension = 8;
            meta.num = 10000;
            FAISSDescriptor descriptor = FAISSDescriptorFactory.make(meta);
            Assert.assertEquals("IDMap,Flat", descriptor.toString());
        }
        {
            FAISSIndexMeta meta = new FAISSIndexMeta();
            meta.dimension = 8;
            meta.num = 100000;
            FAISSDescriptor descriptor = FAISSDescriptorFactory.make(meta);
            Assert.assertEquals("IVF1264,Flat", descriptor.toString());
            Assert.assertEquals(3, descriptor.parameterMap.size());
            Assert.assertEquals(1264, (int) descriptor.parameterMap.get(FAISSConstants.QUANTIZE_NUM));
            Assert.assertEquals(1264, (int) descriptor.parameterMap.get(FAISSConstants.CENTROID_NUM));
            Assert.assertEquals(26, (int) descriptor.parameterMap.get(FAISSConstants.N_PROBE));
        }
        {
            FAISSIndexMeta meta = new FAISSIndexMeta();
            meta.dimension = 32;
            meta.num = 500000;
            meta.parameterMap.put(FAISSConstants.ENCODE_BITS, 8);
            meta.parameterMap.put(FAISSConstants.SUB_QUANTIZE_NUM, 16);
            FAISSDescriptor descriptor = FAISSDescriptorFactory.make(meta);
            Assert.assertEquals("IVF2828,PQ16", descriptor.toString());
            Assert.assertEquals(3, descriptor.parameterMap.size());
            Assert.assertEquals(2828, (int) descriptor.parameterMap.get(FAISSConstants.QUANTIZE_NUM));
            Assert.assertEquals(2828, (int) descriptor.parameterMap.get(FAISSConstants.CENTROID_NUM));
            Assert.assertEquals(58, (int) descriptor.parameterMap.get(FAISSConstants.N_PROBE));
        }
        {
            FAISSIndexMeta meta = new FAISSIndexMeta();
            meta.dimension = 32;
            meta.num = 2000000;
            meta.parameterMap.put(FAISSConstants.ENCODE_BITS, 12);
            meta.parameterMap.put(FAISSConstants.SUB_QUANTIZE_NUM, 16);
            FAISSDescriptor descriptor = FAISSDescriptorFactory.make(meta);
            Assert.assertEquals("IMI2x6,PQ16x12", descriptor.toString());
            Assert.assertEquals(3, descriptor.parameterMap.size());
            Assert.assertEquals(4096, (int) descriptor.parameterMap.get(FAISSConstants.QUANTIZE_NUM));
            Assert.assertEquals(64, (int) descriptor.parameterMap.get(FAISSConstants.CENTROID_NUM));
            Assert.assertEquals(41, (int) descriptor.parameterMap.get(FAISSConstants.N_PROBE));
        }
        {
            FAISSIndexMeta meta = new FAISSIndexMeta();
            meta.dimension = 32;
            meta.num = 20000000;
            meta.parameterMap.put(FAISSConstants.ENCODE_BITS, 8);
            meta.parameterMap.put(FAISSConstants.SUB_QUANTIZE_NUM, 16);
            FAISSDescriptor descriptor = FAISSDescriptorFactory.make(meta);
            Assert.assertEquals("IMI2x9,PQ16", descriptor.toString());
            Assert.assertEquals(3, descriptor.parameterMap.size());
            Assert.assertEquals(262144, (int) descriptor.parameterMap.get(FAISSConstants.QUANTIZE_NUM));
            Assert.assertEquals(512, (int) descriptor.parameterMap.get(FAISSConstants.CENTROID_NUM));
            Assert.assertEquals(512, (int) descriptor.parameterMap.get(FAISSConstants.N_PROBE));
        }
    }
}
