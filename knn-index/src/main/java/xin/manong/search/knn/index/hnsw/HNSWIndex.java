package xin.manong.search.knn.index.hnsw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xin.manong.search.knn.index.KNNIndex;
import xin.manong.search.knn.index.KNNResult;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.Lock;

/**
 * HNSW索引
 *
 * @author frankcl
 * @date 2023-01-10 20:47:01
 */
public class HNSWIndex extends KNNIndex {

    private final static Logger logger = LoggerFactory.getLogger(HNSWIndex.class);

    static {
        HNSWLoader.init();
    }

    public HNSWIndex(HNSWIndexMeta meta) {
        super(meta);
    }

    @Override
    public KNNResult[] search(float[] vector, int k) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            if (this.closed) {
                logger.error("knn index[{}] has been closed", meta.path);
                return new KNNResult[0];
            }
            return AccessController.doPrivileged(
                    (PrivilegedAction<KNNResult[]>) () -> search(pointer, vector, k)
            );
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
            HNSWIndexMeta indexMeta = (HNSWIndexMeta) meta;
            if (meta == null || !meta.check()) {
                logger.error("invalid HNSW index meta");
                throw new RuntimeException("invalid HNSW index meta");
            }
            pointer = open(indexMeta.path, indexMeta.efSearch, indexMeta.space);
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
        if (this.closed) {
            logger.warn("knn index[{}] has been closed", meta.path);
            return;
        }
        try {
            close(pointer);
        } finally {
            this.closed = true;
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
        memorySize = fileSize;
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
     * @param efSearch HNSW参数efSearch
     * @param space HNSW参数space，向量距离空间
     * @return 成功返回指针，否则返回0L
     */
    private native long open(String path, int efSearch, String space);

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
