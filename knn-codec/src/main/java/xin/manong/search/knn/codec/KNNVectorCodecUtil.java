package xin.manong.search.knn.codec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.IOUtils;
import xin.manong.search.knn.common.KNNConstants;

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

    /**
     * 根据索引数据文件名构建meta文件名
     *
     * @param indexDataFileName 索引数据文件名
     * @return 索引meta文件名
     */
    public static String buildMetaFileName(String indexDataFileName) {
        if (indexDataFileName.endsWith(KNNConstants.HNSW_VECTOR_INDEX_DATA_EXTENSION) ||
                indexDataFileName.endsWith(KNNConstants.HNSW_VECTOR_INDEX_DATA_EXTENSION +
                        KNNConstants.COMPOUND_EXTENSION)) {
            return String.format("%s%s", indexDataFileName.substring(0,
                    indexDataFileName.lastIndexOf(KNNConstants.HNSW_VECTOR_INDEX_DATA_EXTENSION)),
                    KNNConstants.HNSW_VECTOR_INDEX_META_EXTENSION);
        }
        if (indexDataFileName.endsWith(KNNConstants.FAISS_VECTOR_INDEX_DATA_EXTENSION) ||
                indexDataFileName.endsWith(KNNConstants.FAISS_VECTOR_INDEX_META_EXTENSION +
                        KNNConstants.COMPOUND_EXTENSION)) {
            return String.format("%s%s", indexDataFileName.substring(0,
                            indexDataFileName.lastIndexOf(KNNConstants.FAISS_VECTOR_INDEX_DATA_EXTENSION)),
                    KNNConstants.FAISS_VECTOR_INDEX_META_EXTENSION);
        }
        logger.error("unexpected index data file name[{}]", indexDataFileName);
        throw new RuntimeException(String.format("unexpected index data file name[%s]", indexDataFileName));
    }

    /**
     * KNN索引文件添加Lucene footer
     *
     * @param tempFileName 临时索引文件名
     * @param fileName 索引文件名
     * @param writeState Lucene segment状态信息
     * @throws IOException
     */
    public static void appendFooter(String tempFileName, String fileName,
                                    SegmentWriteState writeState) throws IOException {
        try (IndexInput is = writeState.directory.openInput(tempFileName, writeState.context);
             IndexOutput os = writeState.directory.createOutput(fileName, writeState.context)) {
            os.copyBytes(is, is.length());
            CodecUtil.writeFooter(os);
        } finally {
            IOUtils.deleteFilesIgnoringExceptions(writeState.directory, tempFileName);
        }
    }
}
