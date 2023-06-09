package xin.manong.search.knn.shard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.FilterLeafReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FilterDirectory;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.IndexShard;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.codec.KNNUtil;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.index.KNNIndex;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.faiss.FAISSIndexMeta;
import xin.manong.search.knn.index.hnsw.HNSWIndexMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * KNN索引shard管理
 *
 * @author frankcl
 * @date 2023-05-22 19:47:53
 */
public class KNNIndexShard {

    private static final Logger logger = LogManager.getLogger(KNNIndexShard.class);

    private IndexShard indexShard;

    public KNNIndexShard(IndexShard indexShard) {
        this.indexShard = indexShard;
    }

    /**
     * 获取KNN向量meta文件
     *
     * @param reader segment reader
     * @return 存在返回meta文件，否则返回null
     * @throws IOException
     */
    private List<String> findKNNVectorMetaFiles(SegmentReader reader) throws IOException {
        List<String> suffixList = new ArrayList<>(), metaFiles = new ArrayList<>();
        suffixList.add(KNNConstants.HNSW_VECTOR_INDEX_META_EXTENSION);
        suffixList.add(KNNConstants.HNSW_VECTOR_INDEX_META_EXTENSION + KNNConstants.COMPOUND_EXTENSION);
        suffixList.add(KNNConstants.FAISS_VECTOR_INDEX_META_EXTENSION);
        suffixList.add(KNNConstants.FAISS_VECTOR_INDEX_META_EXTENSION + KNNConstants.COMPOUND_EXTENSION);
        for (String suffix : suffixList) {
            List<String> fileNames = reader.getSegmentInfo().files().stream()
                    .filter(fileName -> fileName.endsWith(suffix))
                    .collect(Collectors.toList());
            if (!fileNames.isEmpty()) metaFiles.addAll(fileNames);
        }
        return metaFiles;
    }

    /**
     * 读取shard下所有KNN索引meta
     *
     * @param indexReader shard索引数据读取器
     * @return KNN索引meta列表
     * @throws IOException
     */
    private List<KNNIndexMeta> readKNNIndexMetas(IndexReader indexReader) throws IOException {
        List<KNNIndexMeta> indexMetas = new ArrayList<>();
        for (LeafReaderContext leafReaderContext : indexReader.leaves()) {
            SegmentReader reader = (SegmentReader) FilterLeafReader.unwrap(leafReaderContext.reader());
            String directory = ((FSDirectory) FilterDirectory.unwrap(reader.directory())).getDirectory().toString();
            List<String> metaFiles = findKNNVectorMetaFiles(reader);
            for (String metaFile : metaFiles) {
                String metaFilePath = PathUtils.get(directory, metaFile).toString();
                KNNIndexMeta indexMeta = metaFile.endsWith(KNNConstants.FAISS_VECTOR_INDEX_META_EXTENSION) ||
                        metaFile.endsWith(KNNConstants.FAISS_VECTOR_INDEX_META_EXTENSION + KNNConstants.COMPOUND_EXTENSION) ?
                        KNNUtil.readKNNMeta(metaFilePath, FAISSIndexMeta.class) :
                        KNNUtil.readKNNMeta(metaFilePath, HNSWIndexMeta.class);
                indexMeta.path = PathUtils.get(directory, indexMeta.file).toString();
                indexMetas.add(indexMeta);
            }
        }
        return indexMetas;
    }

    /**
     * 预热shard索引
     *
     * @return 加载索引占用内存大小
     */
    public Long warm() throws IOException {
        logger.info("KNN index[{}] is warming", indexShard.shardId().getIndexName());
        Engine.Searcher searcher = indexShard.acquireSearcher("knn-warm");
        try {
            Long size = 0L;
            List<KNNIndexMeta> indexMetas = readKNNIndexMetas(searcher.getIndexReader());
            for (KNNIndexMeta indexMeta : indexMetas) {
                KNNIndex knnIndex = KNNIndexCache.getInstance().get(indexMeta);
                if (knnIndex != null) size += knnIndex.getMemorySize();
            }
            logger.info("KNN index[{}] has been warmed, load memory size[{}]",
                    indexShard.shardId().getIndexName(), size);
            return size;
        } finally {
            searcher.close();
        }
    }
}
