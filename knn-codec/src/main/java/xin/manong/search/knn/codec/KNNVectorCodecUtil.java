package xin.manong.search.knn.codec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.DocIdSetIterator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * KNN向量编码工具
 *
 * @author frankcl
 * @date 2023-05-10 14:28:10
 */
public class KNNVectorCodecUtil {

    private static final Logger logger = LogManager.getLogger(KNNVectorCodecUtil.class);

    /**
     * 从docValue中解析KNN向量数据
     *
     * @param docValues 向量docValues
     * @return KNN向量数据
     * @throws IOException
     */
    public static KNNVectorFacade parseKNNVectors(BinaryDocValues docValues) throws IOException {
        ArrayList<float[]> vectors = new ArrayList<>();
        ArrayList<Integer> docs = new ArrayList<>();
        for (int id = docValues.nextDoc(); id != DocIdSetIterator.NO_MORE_DOCS; id = docValues.nextDoc()) {
            byte[] values = docValues.binaryValue().bytes;
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(values);
                 ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
                vectors.add((float[]) objectStream.readObject());
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
            docs.add(id);
        }
        return new KNNVectorFacade(docs.stream().mapToInt(Integer::intValue).toArray(),
                vectors.toArray(new float[][]{}));
    }
}
