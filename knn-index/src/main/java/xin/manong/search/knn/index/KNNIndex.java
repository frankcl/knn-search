package xin.manong.search.knn.index;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * knn索引抽象定义，具体索引定义继承该类
 *
 * @author frankcl
 * @date 2023-01-10 17:24:40
 */
public abstract class KNNIndex {

    /**
     * 索引是否关闭
     */
    protected volatile boolean closed;
    /**
     * 索引文件大小，单位Byte
     */
    protected long fileSize;
    /**
     * 索引内存大小，单位Byte
     */
    protected long memorySize;
    /**
     * C/C++指针
     */
    protected long pointer;
    /**
     * 索引元数据
     */
    protected KNNIndexMeta meta;
    /**
     * 索引读写锁
     */
    protected final ReadWriteLock lock;

    public KNNIndex(KNNIndexMeta meta) {
        this.closed = false;
        this.fileSize = 0L;
        this.memorySize = 0L;
        this.pointer = 0L;
        this.meta = meta;
        this.lock = new ReentrantReadWriteLock();
        open();
    }

    /**
     * 获取文件大小，单位Byte
     *
     * @return 文件大小
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * 获取占用内容大小，单位Byte
     *
     * @return 占用内容大小
     */
    public long getMemorySize() {
        return memorySize;
    }

    /**
     * 获取索引元数据
     *
     * @return 索引元数据
     */
    public KNNIndexMeta getMeta() {
        return meta;
    }

    /**
     * 向量搜索：搜索与输入向量最相近的k个结果
     *
     * @param vector 搜索向量
     * @param k 最相近数量k
     * @return 搜索结果，无结果返回空数组
     */
    public abstract KNNResult[] search(final float[] vector, final int k);

    /**
     * 打开索引
     *
     * @return 失败抛出异常
     */
    public abstract void open();

    /**
     * 关闭索引
     */
    public abstract void close();
}
