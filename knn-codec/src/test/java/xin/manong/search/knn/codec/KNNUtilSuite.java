package xin.manong.search.knn.codec;

import org.apache.lucene.util.BytesRef;
import org.junit.Assert;
import org.junit.Test;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.index.faiss.FAISSConstants;
import xin.manong.search.knn.index.faiss.FAISSDescriptor;
import xin.manong.search.knn.index.faiss.FAISSIndexMeta;
import xin.manong.search.knn.index.hnsw.HNSWIndexMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author frankcl
 * @date 2023-05-30 10:35:40
 */
public class KNNUtilSuite {

    @Test
    public void testComputeCosineDistance() {
        {
            float[] vector1 = new float[] {1f, 1f, 1f};
            float[] vector2 = new float[] {3f, 3f, 3f};
            Assert.assertEquals(0f, KNNUtil.computeCosineDistance(vector1, vector2), 0.1f);
        }
        {
            float[] vector1 = new float[] {1f, 1f, 1f};
            float[] vector2 = new float[] {-3f, -3f, -3f};
            Assert.assertEquals(2f, KNNUtil.computeCosineDistance(vector1, vector2), 0.1f);
        }
    }

    @Test
    public void testObjectsToFloatArray() {
        List<Object> objects = new ArrayList<>();
        objects.add(1f);
        objects.add(10f);
        float[] floats = KNNUtil.objectsToFloatArray(objects);
        Assert.assertEquals(2, floats.length);
        Assert.assertEquals(1f, floats[0], 0.1f);
        Assert.assertEquals(10f, floats[1], 0.1f);
    }

    @Test
    public void testFloatArrayByteRefConversion() throws IOException {
        float[] floatArray = new float[] { 1f, 2f, 3f };
        byte[] byteArray = KNNUtil.floatArrayToByteArray(floatArray);
        BytesRef bytesRef = new BytesRef(byteArray);
        float[] floats = KNNUtil.byteRefToFloatArray(bytesRef);
        Assert.assertEquals(3, floats.length);
        Assert.assertEquals(1f, floats[0], 0.1f);
        Assert.assertEquals(2f, floats[1], 0.1f);
        Assert.assertEquals(3f, floats[2], 0.1f);
    }

    @Test
    public void testBuildMetaFileName() throws IOException {
        Assert.assertEquals("index.hvm", KNNUtil.buildMetaFileName("index.hvd"));
        Assert.assertEquals("index.hvm", KNNUtil.buildMetaFileName("index.hvdc"));
        Assert.assertEquals("index.fvm", KNNUtil.buildMetaFileName("index.fvd"));
        Assert.assertEquals("index.fvm", KNNUtil.buildMetaFileName("index.fvdc"));
    }

    @Test
    public void testReadWriteHNSWIndexMeta() throws IOException {
        String path = "./feature.hvm";
        HNSWIndexMeta indexMeta = new HNSWIndexMeta();
        indexMeta.num = 100;
        indexMeta.dimension = 12;
        indexMeta.field = "feature";
        indexMeta.file = "feature.hvd";
        indexMeta.path = "./feature.hvd";
        indexMeta.space = KNNConstants.HNSW_SPACE_COSINE;
        indexMeta.efSearch = 32;
        indexMeta.efConstruction = 32;
        indexMeta.M = 16;
        indexMeta.indexThreadQty = 2;
        KNNUtil.writeKNNMeta(indexMeta, path);

        indexMeta = KNNUtil.readKNNMeta(path, HNSWIndexMeta.class);
        Assert.assertEquals(100, indexMeta.num);
        Assert.assertEquals(12, indexMeta.dimension);
        Assert.assertEquals("feature", indexMeta.field);
        Assert.assertEquals("feature.hvd", indexMeta.file);
        Assert.assertEquals(KNNConstants.HNSW_SPACE_COSINE, indexMeta.space);
        Assert.assertEquals(32, indexMeta.efSearch);
        Assert.assertEquals(32, indexMeta.efConstruction);
        Assert.assertEquals(16, indexMeta.M);
        Assert.assertEquals(2, indexMeta.indexThreadQty);
        Assert.assertTrue(indexMeta.path == null);

        new File(path).delete();
    }

    @Test
    public void testReadWriteFAISSIndexMeta() throws IOException {
        String path = "./feature.fvm";
        FAISSIndexMeta indexMeta = new FAISSIndexMeta();
        indexMeta.num = 100000;
        indexMeta.dimension = 8;
        indexMeta.field = "feature";
        indexMeta.file = "feature.hvd";
        indexMeta.path = "./feature.hvd";
        indexMeta.descriptor = new FAISSDescriptor();
        indexMeta.descriptor.search = "IVF1264";
        indexMeta.descriptor.encode = "Flat";
        indexMeta.descriptor.parameterMap.put(FAISSConstants.QUANTIZE_NUM, 1264);
        indexMeta.descriptor.parameterMap.put(FAISSConstants.CENTROID_NUM, 1264);
        indexMeta.descriptor.parameterMap.put(FAISSConstants.N_PROBE, 26);
        indexMeta.parameterMap.put(FAISSConstants.M, 16);
        KNNUtil.writeKNNMeta(indexMeta, path);

        indexMeta = KNNUtil.readKNNMeta(path, FAISSIndexMeta.class);
        Assert.assertEquals(100000, indexMeta.num);
        Assert.assertEquals(8, indexMeta.dimension);
        Assert.assertEquals("feature", indexMeta.field);
        Assert.assertEquals("feature.hvd", indexMeta.file);
        Assert.assertEquals("IVF1264", indexMeta.descriptor.search);
        Assert.assertEquals("Flat", indexMeta.descriptor.encode);
        Assert.assertEquals(1, indexMeta.parameterMap.size());
        Assert.assertEquals(16, indexMeta.parameterMap.get(FAISSConstants.M));
        Assert.assertEquals(3, indexMeta.descriptor.parameterMap.size());
        Assert.assertEquals(1264, indexMeta.descriptor.parameterMap.get(FAISSConstants.QUANTIZE_NUM));
        Assert.assertEquals(1264, indexMeta.descriptor.parameterMap.get(FAISSConstants.CENTROID_NUM));
        Assert.assertEquals(26, indexMeta.descriptor.parameterMap.get(FAISSConstants.N_PROBE));
        Assert.assertTrue(indexMeta.path == null);

        new File(path).delete();
    }
}
