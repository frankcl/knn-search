package xin.manong.search.knn.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.codecs.CodecUtil;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.IOUtils;
import org.elasticsearch.common.Strings;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.index.KNNIndexMeta;

import java.io.*;
import java.nio.charset.Charset;
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
     * 写入KNN索引meta信息
     *
     * @param indexMeta KNN索引meta
     * @param path 文件路径
     * @throws IOException
     */
    public static void writeKNNMeta(KNNIndexMeta indexMeta, String path) throws IOException {
        byte[] bytes = JSON.toJSONString(indexMeta, SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.PrettyFormat).getBytes(Charset.forName("UTF-8"));
        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(path))) {
            output.writeInt(bytes.length);
            output.write(bytes, 0, bytes.length);
        }
    }

    /**
     * 读取KNN索引meta信息
     *
     * @param path 文件路径
     * @param c meta类信息
     * @return KNN索引meta
     * @param <T>
     * @throws IOException
     */
    public static <T extends KNNIndexMeta> T readKNNMeta(String path, Class<T> c) throws IOException {
        try (DataInputStream input = new DataInputStream(new FileInputStream(path))){
            byte[] bytes = new byte[input.read()];
            input.read(bytes, 0, bytes.length);
            return JSON.parseObject(new String(bytes, Charset.forName("UTF-8")), c);
        }
    }

    /**
     * KNN索引文件添加Lucene footer
     *
     * @param tempFileName 临时索引文件名
     * @param fileName 索引文件名
     * @param directory 目录
     * @param context 上下文
     * @throws IOException
     */
    public static void appendFooter(String tempFileName, String fileName,
                                    Directory directory, IOContext context) throws IOException {
        try (IndexInput is = directory.openInput(tempFileName, context);
             IndexOutput os = directory.createOutput(fileName, context)) {
            os.copyBytes(is, is.length());
            CodecUtil.writeFooter(os);
        } finally {
            IOUtils.deleteFilesIgnoringExceptions(directory, tempFileName);
        }
    }
}
