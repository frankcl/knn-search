package xin.manong.search.knn.codec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocIDMerger;
import org.apache.lucene.index.MergeState;

import java.io.IOException;

/**
 * KNN向量docValues读取器
 *
 * @author frankcl
 * @date 2023-05-10 10:59:33
 */
public class KNNVectorDocValuesSub extends DocIDMerger.Sub {

    private static final Logger logger = LogManager.getLogger(KNNVectorDocValuesSub.class);

    private BinaryDocValues values;

    /**
     * 构造函数
     *
     * @param docMap docMap
     * @param values vector doc values
     */
    protected KNNVectorDocValuesSub(MergeState.DocMap docMap, BinaryDocValues values) {
        super(docMap);
        if (values == null || (values.docID() != -1)) {
            logger.error("vector doc values is null or doc id is not -1");
            throw new IllegalStateException("vector doc values is either null or doc id is not -1");
        }
        this.values = values;
    }

    @Override
    public int nextDoc() throws IOException {
        return values.nextDoc();
    }

    /**
     * 获取向量docValues
     *
     * @return 向量docValues
     */
    public BinaryDocValues getValues() {
        return values;
    }
}
