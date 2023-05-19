package xin.manong.search.knn.codec;

import org.apache.lucene.codecs.CompoundFormat;
import org.apache.lucene.codecs.DocValuesFormat;

/**
 * KNN向量 format facade
 *
 * @author frankcl
 * @date 2023-05-10 15:59:38
 */
public class KNNVectorFormatFacade {

    private final DocValuesFormat docValuesFormat;
    private final CompoundFormat compoundFormat;

    public KNNVectorFormatFacade(final DocValuesFormat docValuesFormat, final CompoundFormat compoundFormat) {
        this.docValuesFormat = docValuesFormat;
        this.compoundFormat = compoundFormat;
    }

    public DocValuesFormat docValuesFormat() {
        return docValuesFormat;
    }

    public CompoundFormat compoundFormat() {
        return compoundFormat;
    }
}
