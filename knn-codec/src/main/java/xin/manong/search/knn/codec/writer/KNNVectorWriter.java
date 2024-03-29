package xin.manong.search.knn.codec.writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.SegmentWriteState;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FilterDirectory;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.codec.KNNUtil;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.common.KNNSettings;
import xin.manong.search.knn.index.KNNIndexData;
import xin.manong.search.knn.index.KNNIndexFactory;
import xin.manong.search.knn.index.KNNIndexMeta;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * KNN向量索引写入接口定义
 *
 * @author frankcl
 * @date 2023-05-10 15:53:04
 */
public abstract class KNNVectorWriter {

    private static final Logger logger = LogManager.getLogger(KNNVectorWriter.class);

    protected KNNIndexFactory indexFactory;

    /**
     * KNN向量meta文件写入
     *
     * @param indexMeta 向量索引meta信息
     * @param writeState lucene索引segment状态信息
     * @throws IOException
     */
    protected void writeMeta(KNNIndexMeta indexMeta,
                             SegmentWriteState writeState) throws IOException {
        String metaFile = KNNUtil.buildMetaFileName(indexMeta.file);
        String tempMetaFile = String.format("%s%s", metaFile, KNNConstants.TEMP_EXTENSION);
        String tempMetaFilePath = Paths.get(((FSDirectory) (FilterDirectory.unwrap(writeState.directory))).
                getDirectory().toString(), tempMetaFile).toString();
        KNNUtil.writeKNNMeta(indexMeta, tempMetaFilePath);
        KNNUtil.appendFooter(tempMetaFile, metaFile, writeState.directory, writeState.context);
    }

    /**
     * KNN向量数据文件写入
     *
     * @param indexData 索引数据
     * @param indexMeta 索引meta
     * @param writeState lucene索引segment状态信息
     * @throws IOException
     */
    protected void writeData(KNNIndexData indexData,
                             KNNIndexMeta indexMeta,
                             SegmentWriteState writeState) throws IOException {
        boolean status = AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                indexFactory.build(indexData, indexMeta));
        if (!status) {
            logger.error("build KNN index failed for index[{}], field[{}] and file[{}]",
                    indexMeta.index, indexMeta.field, indexMeta.file);
            throw new IOException(String.format("build KNN index failed for index[%s], field[%s] and file[%s]",
                    indexMeta.index, indexMeta.field, indexMeta.file));
        }
        String tempFile = Paths.get(indexMeta.path).getFileName().toString();
        KNNUtil.appendFooter(tempFile, indexMeta.file, writeState.directory, writeState.context);
        if (indexMeta.path.endsWith(KNNConstants.TEMP_EXTENSION)) {
            indexMeta.path = indexMeta.path.substring(0, indexMeta.path.length() -
                    KNNConstants.TEMP_EXTENSION.length());
        }
    }

    /**
     * 获取索引参数
     * 1. 从字段配置中获取
     * 2. 从索引配置中获取
     *
     * @param fieldInfo 字段信息
     * @param key 参数key
     * @return 参数值
     */
    protected Integer getIndexParameter(FieldInfo fieldInfo, String key) {
        if (fieldInfo.attributes().containsKey(key)) return Integer.parseInt(fieldInfo.attributes().get(key));
        String index = fieldInfo.attributes().get(KNNConstants.FIELD_ATTRIBUTE_INDEX);
        Integer v = (Integer) KNNSettings.getKNNIndexParameter(index, key);
        if (v == null) throw new RuntimeException(String.format("index parameter[%s] is not found", key));
        return v;
    }

    /**
     * KNN向量写入
     *
     * @param indexData 向量数据
     * @param writeState segment信息
     * @param field 字段信息
     */
    public void write(KNNIndexData indexData,
                      SegmentWriteState writeState,
                      FieldInfo field) throws IOException {
        Long startTime = System.currentTimeMillis();
        KNNIndexMeta indexMeta = buildIndexMeta(indexData, writeState, field);
        writeData(indexData, indexMeta, writeState);
        writeMeta(indexMeta, writeState);
        if (!KNNSettings.isLazyLoad()) KNNIndexCache.getInstance().get(indexMeta);
        logger.info("build KNN index success for index[{}], field[{}] and file[{}], spend time[{}]",
                indexMeta.index, indexMeta.field, indexMeta.file, System.currentTimeMillis() - startTime);
    }

    /**
     * 构建KNN索引meta信息
     *
     * @param indexData 向量数据
     * @param writeState segment信息
     * @param field 字段信息
     * @return KNN索引meta信息
     */
    public abstract KNNIndexMeta buildIndexMeta(KNNIndexData indexData,
                                                SegmentWriteState writeState,
                                                FieldInfo field);
}
