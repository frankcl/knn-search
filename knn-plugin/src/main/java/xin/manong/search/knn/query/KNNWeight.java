package xin.manong.search.knn.query;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.FilterLeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SegmentReader;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FilterDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.DocIdSetBuilder;
import org.elasticsearch.core.PathUtils;
import xin.manong.search.knn.cache.KNNIndexCache;
import xin.manong.search.knn.codec.KNNVectorCodecUtil;
import xin.manong.search.knn.common.KNNConstants;
import xin.manong.search.knn.index.KNNIndex;
import xin.manong.search.knn.index.KNNIndexMeta;
import xin.manong.search.knn.index.KNNResult;
import xin.manong.search.knn.index.faiss.FAISSIndexMeta;
import xin.manong.search.knn.index.hnsw.HNSWIndexMeta;
import xin.manong.search.knn.util.KNNUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * KNN搜索结果排序及权重
 *
 * @author frankcl
 * @date 2023-05-17 13:42:13
 */
public class KNNWeight extends Weight {

    private static final Logger logger = LogManager.getLogger(KNNWeight.class);

    private float boost;
    private KNNQuery query;

    public KNNWeight(KNNQuery query, float boost) {
        super(query);
        this.query = query;
        this.boost = boost;
    }


    @Override
    public Explanation explain(LeafReaderContext context, int doc) throws IOException {
        return Explanation.match(1.0f, "no explanation");
    }

    @Override
    public Scorer scorer(LeafReaderContext context) throws IOException {
        SegmentReader reader = (SegmentReader) FilterLeafReader.unwrap(context.reader());
        String directory = ((FSDirectory) FilterDirectory.unwrap(reader.directory())).getDirectory().toString();
        String metaFile = findKNNVectorMetaFile(reader);
        if (StringUtils.isEmpty(metaFile)) return null;
        String metaFilePath = PathUtils.get(directory, metaFile).toString();
        KNNIndexMeta indexMeta = metaFile.endsWith(KNNConstants.FAISS_VECTOR_INDEX_META_EXTENSION) ?
                KNNVectorCodecUtil.readKNNMeta(metaFilePath, FAISSIndexMeta.class) :
                KNNVectorCodecUtil.readKNNMeta(metaFilePath, HNSWIndexMeta.class);
        indexMeta.path = PathUtils.get(directory, indexMeta.file).toString();
        KNNIndex index = KNNIndexCache.getInstance().get(indexMeta);
        KNNResult[] results = index.search(query.vector, query.k);
        if (indexMeta instanceof FAISSIndexMeta) reRanking(results, reader);
        return buildKNNScorer(results);
    }

    @Override
    public boolean isCacheable(LeafReaderContext context) {
        return true;
    }

    /**
     * 构建KNN排序scorer
     *
     * @param results 搜索结果
     * @return KNN排序scorer
     * @throws IOException
     */
    protected KNNScorer buildKNNScorer(final KNNResult[] results) throws IOException {
        Map<Integer, Float> docScoreMap = Arrays.stream(results).collect(
                Collectors.toMap(result -> result.id, result -> 1 / (1 + result.score)));
        int maxDocID = Collections.max(docScoreMap.keySet()) + 1;
        DocIdSetBuilder docIdSetBuilder = new DocIdSetBuilder(maxDocID);
        DocIdSetBuilder.BulkAdder setAdder = docIdSetBuilder.grow(maxDocID);
        Arrays.stream(results).forEach(result -> setAdder.add(result.id));
        DocIdSetIterator iterator = docIdSetBuilder.build().iterator();
        return new KNNScorer(this, iterator, docScoreMap, boost);
    }

    /**
     * 重新计算分数
     * 保证FAISS索引和HNSW索引算分标准一致
     *
     * @param results 搜索结果列表
     * @param reader segment reader
     * @throws IOException
     */
    private void reRanking(KNNResult[] results, SegmentReader reader) throws IOException {
        for (int i = 0; i < results.length; i++) {
            KNNResult result = results[i];
            BinaryDocValues docValues = reader.getBinaryDocValues(query.field);
            if (!docValues.advanceExact(result.id)) {
                logger.error("advanced locating doc id[{}] failed", result.id);
                throw new IOException(String.format("advanced locating doc id[%d] failed", result.id));
            }
            BytesRef bytesRef = docValues.binaryValue();
            float[] vector = KNNVectorCodecUtil.byteRefToFloatArray(bytesRef);
            result.score = KNNUtil.cosineScore(query.vector, vector);
        }
    }

    /**
     * 获取KNN向量meta文件
     *
     * @param reader segment reader
     * @return 存在返回meta文件，否则返回null
     * @throws IOException
     */
    private String findKNNVectorMetaFile(SegmentReader reader) throws IOException {
        List<String> suffixList = new ArrayList<>();
        suffixList.add(String.format("_%s%s", query.field, KNNConstants.HNSW_VECTOR_INDEX_META_EXTENSION));
        suffixList.add(String.format("_%s%s", query.field, KNNConstants.FAISS_VECTOR_INDEX_META_EXTENSION));
        for (String suffix : suffixList) {
            List<String> metaFiles = reader.getSegmentInfo().files().stream()
                    .filter(fileName -> fileName.endsWith(suffix))
                    .collect(Collectors.toList());
            if (!metaFiles.isEmpty()) return metaFiles.get(0);
        }
        logger.warn("can not find KNN vector meta file for field[{}] and index[{}]", query.field, query.index);
        return null;
    }
}
