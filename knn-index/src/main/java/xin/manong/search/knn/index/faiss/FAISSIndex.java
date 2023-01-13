package xin.manong.search.knn.index.faiss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.search.knn.index.KNNIndex;
import xin.manong.search.knn.index.KNNResult;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.Lock;

/**
 * FAISS索引
 *
 * @author frankcl
 * @date 2023-01-10 20:19:19
 */
public class FAISSIndex extends KNNIndex {

    private final static Logger logger = LoggerFactory.getLogger(FAISSIndex.class);

    static {
        FAISSLoader.load();
    }

    public FAISSIndex(FAISSIndexMeta meta) {
        super(meta);
    }

    @Override
    public KNNResult[] search(float[] vector, int k) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            if (closed) {
                logger.error("knn index[{}] has been closed", meta.path);
                return new KNNResult[0];
            }
            return AccessController.doPrivileged((
                    PrivilegedAction<KNNResult[]>) () -> search(pointer, vector, k));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new KNNResult[0];
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void open() {
        try {
            pointer = open(meta.path);
            compute();
        } catch (Exception e) {
            logger.error("open knn index[{}] failed for type[{}]", meta.path, meta.type.name());
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (pointer == 0L) {
            logger.warn("knn index is not init");
            return;
        }
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        if (closed) {
            logger.warn("knn index[{}] has been closed", meta.path);
            return;
        }
        try {
            close(pointer);
        } finally {
            closed = true;
            writeLock.unlock();
        }
    }

    /**
     * 计算knn索引占用空间
     */
    private void compute() {
        File file = new File(meta.path);
        if (!file.exists() || !file.isFile()) {
            logger.warn("knn index[{}] is not found or not a file", meta.path);
            return;
        }
        fileSize = file.length() / 1024 + 1;
        FAISSIndexMeta indexMeta = (FAISSIndexMeta) meta;
//        if (indexMeta.naming.encodeComponent != null && indexMeta.naming.encodeComponent.
//                startsWith(FAISSIndexNameParser.COMPONENT_ENCODE_PQ)) {
//            memorySize = indexMeta.dataNum * faissMeta.subQuantizerNum * indexMeta.encodeBits / 8;
//            memorySize += Math.pow(2, indexMeta.encodeBits) * indexMeta.dimension * 4;
//        } else {
//            memorySize = indexMeta.dataNum * indexMeta.dimension * 4;
//        }
//        if (indexMeta.naming.searchComponent != null) {
//            if (indexMeta.naming.searchComponent.startsWith(FAISSIndexNameParser.COMPONENT_SEARCH_IVF)) {
//                memorySize += indexMeta.quantizerNum * indexMeta.dimension * 4;
//            } else if (indexMeta.naming.searchComponent.startsWith(FAISSIndexNameParser.COMPONENT_SEARCH_IMI)) {
//                memorySize += indexMeta.centroidsPerSegment * indexMeta.dimension * 4;
//            } else if (indexMeta.naming.searchComponent.startsWith(FAISSIndexNameParser.COMPONENT_SEARCH_HNSW)) {
//                memorySize += indexMeta.dataNum * 8 * indexMeta.M;
//            }
//        }
//        if (indexMeta.naming.encodeComponent != null && indexMeta.naming.encodeComponent.
//                startsWith(FAISSIndexNameParser.COMPONENT_ENCODE_PQ) && indexMeta.naming.searchComponent != null) {
//            if (indexMeta.naming.searchComponent.startsWith(FAISSIndexNameParser.COMPONENT_SEARCH_IVF)) {
//                memorySize *= 4;
//            } else if (indexMeta.naming.searchComponent.startsWith(FAISSIndexNameParser.COMPONENT_SEARCH_IMI)) {
//                memorySize *= 2;
//            }
//        }
        memorySize = memorySize / 1024 + 1;
    }

    /**
     * 本地方法：关闭knn索引
     *
     * @param pointer C/C++指针
     */
    private native void close(long pointer);

    /**
     * 本地方法：打开knn索引
     *
     * @param path 索引文件路径
     * @return 成功返回指针，否则返回0L
     */
    private native long open(String path);

    /**
     * 本地方法：搜索向量，返回最相似k个结果
     *
     * @param pointer knn索引指针
     * @param vector 搜索向量
     * @param k 最相近数量k
     * @return 搜索列表，无结果返回空数组
     */
    private native KNNResult[] search(long pointer, float[] vector, int k);
}
