package xin.manong.search.knn.index.faiss;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final static Logger logger = LogManager.getLogger(FAISSIndex.class);

    static {
        FAISSLoader.init();
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
        if (closed) {
            logger.warn("knn index[{}] has been closed", meta.path);
            return;
        }
        Lock writeLock = lock.writeLock();
        writeLock.lock();
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
        FAISSDescriptor descriptor = indexMeta.descriptor;
        memorySize = indexMeta.num * indexMeta.dimension * 4;
        if (descriptor.encode != null && descriptor.encode.startsWith(FAISSConstants.COMPONENT_ENCODE_PQ)) {
            int encodeBits = (int) indexMeta.parameterMap.get(FAISSConstants.ENCODE_BITS);
            int subQuantizeNum = (int) indexMeta.parameterMap.get(FAISSConstants.SUB_QUANTIZE_NUM);
            memorySize = indexMeta.num * subQuantizeNum * encodeBits / 8;
            memorySize += Math.pow(2, encodeBits) * indexMeta.dimension * 4;
        }
        if (descriptor.search != null) {
            if (descriptor.search.startsWith(FAISSConstants.COMPONENT_SEARCH_IVF)) {
                int quantizeNum = (int) descriptor.parameterMap.get(FAISSConstants.QUANTIZE_NUM);
                memorySize += quantizeNum * indexMeta.dimension * 4;
            } else if (descriptor.search.startsWith(FAISSConstants.COMPONENT_SEARCH_IMI)) {
                int centroidNum = (int) descriptor.parameterMap.get(FAISSConstants.CENTROID_NUM);
                memorySize += centroidNum * indexMeta.dimension * 4;
            } else if (descriptor.search.startsWith(FAISSConstants.COMPONENT_SEARCH_HNSW)) {
                int M = (int) indexMeta.parameterMap.get(FAISSConstants.M);
                memorySize += indexMeta.num * 8 * M;
            }
        }
        if (descriptor.search != null && descriptor.encode != null &&
                descriptor.encode.startsWith(FAISSConstants.COMPONENT_ENCODE_PQ)) {
            if (descriptor.search.startsWith(FAISSConstants.COMPONENT_SEARCH_IVF)) memorySize *= 4;
            else if (descriptor.search.startsWith(FAISSConstants.COMPONENT_SEARCH_IMI)) memorySize *= 2;
        }
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
